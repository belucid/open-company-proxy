(ns oc.proxy.config
  "Namespace for the configuration parameters."
  (:require [environ.core :refer (env)]))

(defn- bool
  "Handle the fact that we may have true/false strings, when we want booleans."
  [val]
  (boolean (Boolean/valueOf val)))

;; ----- System -----

(defonce processors (.availableProcessors (Runtime/getRuntime)))
(defonce core-async-limit (+ 42 (* 2 processors)))

(defonce prod? (= "production" (env :env)))

;; ----- Sentry -----

(defonce dsn (or (env :open-company-sentry-proxy) false))

;; ----- Logging (see https://github.com/ptaoussanis/timbre) -----

(defonce log-level (or (env :log-level) :info))

;; ----- HTTP server -----

(defonce hot-reload (bool (or (env :hot-reload) false)))
(defonce proxy-server-port (Integer/parseInt (or (env :port) "3007")))