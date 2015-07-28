(ns govtrack-sync-clj.main
  (:require [govtrack-sync-clj.legislators.legislators :as leg]
            [clojure.tools.logging :as log]))

(defn main [&args]
  (let [config &args]
    (log/info (str "Starting Sync Job against the following resources" config))
    (leg/persist-legislators config "test-resources/legislators/legislators-current.yaml" "congress" "legislator")))
