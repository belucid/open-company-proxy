(defproject open-company-proxy "0.1.0-SNAPSHOT"
  :description "OpenCompany Proxy Service"
  :url "https://github.com/open-company/open-company-proxy"
  :license {
    :name "Mozilla Public License v2.0"
    :url "http://www.mozilla.org/MPL/2.0/"
  }

  :min-lein-version "2.7.1"
  
  ;; JVM memory
  :jvm-opts ^:replace ["-Xms256m" "-Xmx2048m" "-server"]

  ;; All profile dependencies
  :dependencies [
    [org.clojure/clojure "1.9.0-alpha17"] ; Lisp on the JVM http://clojure.org/documentation
    [http-kit "2.3.0-alpha2"] ; Web server http://http-kit.org/
    [ring/ring-devel "1.6.2"] ; Web application library https://github.com/ring-clojure/ring
    [ring/ring-core "1.6.2"] ; Web application library https://github.com/ring-clojure/ring
    [ring-logger-timbre "0.7.5"] ; Ring logging https://github.com/nberger/ring-logger-timbre
    [compojure "1.6.0"] ; Web routing https://github.com/weavejester/compojure

    [open-company/lib "0.11.20"]] ; Library for OC projects https://github.com/open-company/open-company-lib
    ; In addition to common functions, brings in the following common dependencies used by this project:
    ; Component - Component Lifecycle https://github.com/stuartsierra/component
    ; Timbre - Pure Clojure/Script logging library https://github.com/ptaoussanis/timbre
    ; Raven - Interface to Sentry error reporting https://github.com/sethtrain/raven-clj
    ; environ - Get environment settings from different sources https://github.com/weavejester/environ

  ;; All profile plugins
  :plugins [
    [lein-ring "0.12.0"] ; Common ring tasks https://github.com/weavejester/lein-ring
    [lein-environ "1.1.0"] ; Get environment settings from different sources https://github.com/weavejester/environ
  ]

  :profiles {

    ;; QA environment and dependencies
    :qa {
      :env {
        :hot-reload "false"
      }
      :plugins [
        [jonase/eastwood "0.2.4"] ; Linter https://github.com/jonase/eastwood
        [lein-kibit "0.1.6-beta1"] ; Static code search for non-idiomatic code https://github.com/jonase/kibit
      ]
    }

    ;; Dev environment and dependencies
    :dev [:qa {
      :env ^:replace {
        :hot-reload "true" ; reload code when changed on the file system
      }
      :plugins [
        [lein-bikeshed "0.4.1"] ; Check for code smells https://github.com/dakrone/lein-bikeshed
        [lein-checkall "0.1.1"] ; Runs bikeshed, kibit and eastwood https://github.com/itang/lein-checkall
        [lein-pprint "1.1.2"] ; pretty-print the lein project map https://github.com/technomancy/leiningen/tree/master/lein-pprint
        [lein-ancient "0.6.10"] ; Check for outdated dependencies https://github.com/xsc/lein-ancient
        [lein-spell "0.1.0"] ; Catch spelling mistakes in docs and docstrings https://github.com/cldwalker/lein-spell
        [lein-deps-tree "0.1.2"] ; Print a tree of project dependencies https://github.com/the-kenny/lein-deps-tree
        [venantius/yagni "0.1.4"] ; Dead code finder https://github.com/venantius/yagni
      ]  
    }]
    :repl-config [:dev {
      :dependencies [
        [org.clojure/tools.nrepl "0.2.13"] ; Network REPL https://github.com/clojure/tools.nrepl
        [aprint "0.1.3"] ; Pretty printing in the REPL (aprint ...) https://github.com/razum2um/aprint
      ]
      ;; REPL injections
      :injections [
        (require '[aprint.core :refer (aprint ap)]
                 '[clojure.stacktrace :refer (print-stack-trace)]
                 '[clojure.string :as s]
                 )
      ]
    }]

    ;; Production environment
    :prod {
      :env {
        :env "production"
        :hot-reload "false"
      }
    }
  }

  :repl-options {
    :welcome (println (str "\n" (slurp (clojure.java.io/resource "ascii_art.txt")) "\n"
                      "OpenCompany Proxy\n"
                      "\nReady to do your bidding...\n"))
  }

  :aliases {
    "build" ["do" "clean," "deps," "compile"] ; clean and build code
    "start" ["run"] ; start a development server
    "repl" ["with-profile" "+repl-config" "repl"]
    "spell!" ["spell" "-n"] ; check spelling in docs and docstrings
    "bikeshed!" ["bikeshed" "-v" "-m" "120"] ; code check with max line length warning of 120 characters
    "ancient" ["ancient" ":all" ":allow-qualified"] ; check for out of date dependencies
  }

  ;; ----- Code check configuration -----

  :eastwood {
    ;; Disable some linters that are enabled by default
    :exclude-linters [:constant-test :wrong-arity]
    ;; Enable some linters that are disabled by default
    :add-linters [:unused-namespaces :unused-private-vars] ; :unused-locals]

    ;; Exclude testing namespaces
    :tests-paths ["test"]
    :exclude-namespaces [:test-paths]
  }

  ;; ----- API -----

  :ring {
    :handler oc.proxy.app/app
    :reload-paths ["src"] ; work around issue https://github.com/weavejester/lein-ring/issues/68
  }

  :main oc.proxy.app
)
