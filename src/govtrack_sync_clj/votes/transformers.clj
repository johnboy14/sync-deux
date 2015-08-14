(ns govtrack-sync-clj.votes.transformers
  (:require [cheshire.core :as ch]))

(defn extract-bill-id [bill]
  (str (:type bill) (:number bill) "-" (:congress bill)))

(defn set-bill-id [vote]
  (assoc vote :bill_id (extract-bill-id (:bill vote))))

(defn set-vote-counts [vote]
  (-> vote
      (assoc :nay (count (:Nay (:votes vote))))
      (assoc :absent (count ((keyword "Not Voting") (:votes vote))))
      (assoc :yea (count (:Yea (:votes vote))))))

(defn retrieve-vote-details [vote]
  (-> vote
      (set-bill-id)
      (dissoc :bill)
      (set-vote-counts)
      (dissoc :votes)
      (dissoc :amendment)
      ((fn [m] (into {} (remove (fn [[_ v]] (nil? v)) m))))))

(defn construct-neo-vote-payload [vote]
  (if (contains? vote :drained)
    vote
    (-> {}
        (assoc :vote-details (retrieve-vote-details vote))
        (assoc :votes (:votes vote)))))

(def vote-neo-transformer
  (comp
    (map #(ch/parse-string % true))
    (map #(construct-neo-vote-payload %))))
