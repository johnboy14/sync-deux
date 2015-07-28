(ns govtrack-sync-clj.main
  (:require [govtrack-sync-clj.legislators.legislators :as leg]))

(defn main [&args]
  (let [config &args]
    (leg/persist-legislators config "test-resources/legislators/legislators-current.yaml" "congress" "legislator")))
