(ns govtrack-sync-clj.main
  (:gen-class)
  (:require [govtrack-sync-clj.legislators.legislators :as leg]
            [govtrack-sync-clj.bills.bills :as bills]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn]))

(def config (edn/read-string (slurp "resources/config.edn")))

(defn -main [& args]
  (log/info (str "Starting Sync Job against the following resources" config))
  (leg/persist-legislators config (:legislator-dir config) "congress" "legislator")
  (bills/persist-bills config))
