(ns govtrack-sync-clj.bills.transformers
  (:require [cheshire.core :as ch]))

(defn retrieve-bill-details [bill]
  (-> bill
      (dissoc :actions)
      (dissoc :amendments)
      (dissoc :committees)
      (dissoc :cosponsors)
      (dissoc :related_bills)
      (dissoc :history)
      (dissoc :sponsor)
      (dissoc :subjects)
      (dissoc :summary)
      (dissoc :titles)
      (dissoc :enacted_as)
      ((fn [m] (into {} (remove (fn [[_ v]] (nil? v)) m))))))

(def bill-transformer
  (comp
    (map #(ch/parse-string % true))
    (map #(assoc % :_id (:bill_id %)))))

(def bill-neo-transformer
  (comp
    (map #(retrieve-bill-details %))))