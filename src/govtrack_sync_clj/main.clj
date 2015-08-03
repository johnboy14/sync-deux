(ns govtrack-sync-clj.main
  (:gen-class)
  (:require [govtrack-sync-clj.legislators.legislators :as leg]
            [govtrack-sync-clj.bills.bills :as bills]
            [clojure.tools.logging :as log]
            [clojure.edn :as edn]))

(defn -main [& args]
  (let [config (edn/read-string (slurp (first args)))]
    (log/info (str "Starting Sync Job against the following resources" config))
    (leg/persist-legislators config (:legislator-dir config) "congress" "legislator")
    (bills/persist-bills config)))
