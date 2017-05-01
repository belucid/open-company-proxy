(ns oc.proxy.sheets-chart
  "Very simple wrapper around oc.lib.proxy.sheets-chart to proxy to Google Sheets public charts."
  (:require [compojure.core :as compojure :refer (defroutes GET)]
            [oc.lib.proxy.sheets-chart :as sheets-chart]))

(defn- sheets-chart-proxy [sheet-id request]
  (sheets-chart/proxy-sheets (str "/spreadsheets/d/" sheet-id "/pubchart") (:query-params request)))

;; ----- Routes -----

(defn routes [sys]
  (compojure/routes
    (GET "/spreadsheets/d/:sheet-id/pubchart" [sheet-id :as request] (sheets-chart-proxy sheet-id request))))