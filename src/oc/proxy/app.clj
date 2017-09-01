(ns oc.proxy.app
  "Namespace for the web application which serves the proxy."
  (:gen-class)
  (:require
    [raven-clj.core :as sentry]
    [raven-clj.interfaces :as sentry-interfaces]
    [raven-clj.ring :as sentry-mw]
    [taoensso.timbre :as timbre]
    [ring.logger.timbre :refer (wrap-with-logger)]
    [ring.middleware.params :refer (wrap-params)]
    [ring.middleware.reload :refer (wrap-reload)]
    [compojure.core :as compojure :refer (GET)]
    [com.stuartsierra.component :as component]
    [oc.lib.sentry-appender :as sa]
    [oc.lib.api.common :as api-common]
    [oc.proxy.config :as c]
    [oc.proxy.components :as components]
    [oc.proxy.sheets-chart :as sheets-chart]))

;; ----- Unhandled Exceptions -----

;; Send unhandled exceptions to log and Sentry
;; See https://stuartsierra.com/2015/05/27/clojure-uncaught-exceptions
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (timbre/error ex "Uncaught exception on" (.getName thread) (.getMessage ex))
     (when c/dsn
       (sentry/capture c/dsn (-> {:message (.getMessage ex)}
                                 (assoc-in [:extra :exception-data] (ex-data ex))
                                 (sentry-interfaces/stacktrace ex)))))))

;; ----- Request Routing -----

(defn routes [sys]
  (compojure/routes
    (sheets-chart/routes sys)))

;; ----- System Startup -----

;; Ring app definition
(defn app [sys]
  (cond-> (routes sys)
    c/prod?           api-common/wrap-500 ; important that this is first
    c/dsn             (sentry-mw/wrap-sentry c/dsn) ; important that this is second
    c/prod?           wrap-with-logger
    true              wrap-params
    c/hot-reload      wrap-reload))

(defn start
  "Start a development server"
  [port]

  ;; Stuff logged at error level goes to Sentry
  (if c/dsn
    (timbre/merge-config!
      {:level (keyword c/log-level)
       :appenders {:sentry (sa/sentry-appender c/dsn)}})
    (timbre/merge-config! {:level (keyword c/log-level)}))

    ;; Start the system
    (-> {:handler-fn app :port port}
      components/proxy-system
      component/start)

  ;; Echo config information
  (println (str "\n" (slurp (clojure.java.io/resource "ascii_art.txt")) "\n"
    "OpenCompany Proxy Service\n\n"
    "Running on port: " port "\n"
    "Hot-reload: " c/hot-reload "\n"
    "Sentry: " c/dsn "\n\n"
    "Ready to serve...\n")))

(defn -main []
  (start c/proxy-server-port))