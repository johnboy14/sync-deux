(ns govtrack-sync-clj.bills.transformers
  (:gen-class)
  (:require [cheshire.core :as ch]))

(defn set-sponsor [bill]
  (assoc bill :sponsor (:thomas_id (:sponsor bill))))

(defn set-cosponsors [bill]
  (loop [cosponsors (:cosponsors bill)
         acc ""]
    (if (empty? (rest cosponsors))
      (assoc bill :cosponsors (str acc (:thomas_id (first cosponsors))))
      (recur (last cosponsors) (str acc (:thomas_id (first cosponsors)) ",")))))

(defn retrieve-bill-details [bill]
  (-> bill
      (dissoc :actions)
      (dissoc :amendments)
      (dissoc :committees)
      (set-cosponsors)
      (dissoc :related_bills)
      (dissoc :history)
      (set-sponsor)
      ;(dissoc :sponsor)
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