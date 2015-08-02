(ns govtrack-sync-clj.bills.transformers
  (:gen-class)
  (:require [cheshire.core :as ch]))

(defn set-sponsor [bill]
  (assoc bill :sponsor (:thomas_id (:sponsor bill))))

(defn retrieve-bill-details [bill]
  (-> bill
      (dissoc :actions)
      (dissoc :amendments)
      (dissoc :committees)
      (dissoc :related_bills)
      (dissoc :history)
      (set-sponsor)
      (dissoc :cosponsors)
      (dissoc :subjects)
      (dissoc :summary)
      (dissoc :titles)
      (dissoc :enacted_as)
      ((fn [m] (into {} (remove (fn [[_ v]] (nil? v)) m))))))

(defn construct-neo-bill-payload [bill]
    (if (contains? bill :drained)
      bill
      (-> {}
          (assoc :bill-details (retrieve-bill-details bill))
          (assoc :cosponsors (:cosponsors bill)))))

(def es-bill-transformer
  (comp
    (map #(ch/parse-string % true))
    (map #(assoc % :_id (:bill_id %)))))

(def bill-neo-transformer
  (comp
    es-bill-transformer
    (map #(construct-neo-bill-payload %))))