(ns oc.proxy.sheets-chart
  "Very simple wrapper around oc.lib.proxy.sheets-chart to proxy to Google Sheets public charts."
  (:require [compojure.core :as compojure :refer (defroutes GET)]
            [oc.lib.proxy.sheets-chart :as sheets-chart]))

(defn- chart-proxy [path params]
  (sheets-chart/proxy-sheets-chart path params))

(defn- sheets-proxy [path params]
  (sheets-chart/proxy-sheets-pass-through path params))

;; ----- Routes -----

(defn routes [sys]
  (compojure/routes
    (GET "/_/sheets-proxy/ping" [] {:body "OpenCompany Proxy Service: OK" :status 200}) ; Up-time monitor
    (GET ["/_/sheets-proxy/:path" :path #".*"] [path & params] (chart-proxy path params))
    (GET ["/_/sheets-proxy-pass-through/:path" :path #".*"] [path & params] (sheets-proxy path params))))