(ns govtrack-sync-clj.utils.chan-utils
  (:gen-class)
  (:require [clojure.core.async :as async]))

(defn drained? [map]
  (if (contains? map :drained)
    true
    false))

(defn batch [channel batch-size]
  (let [timeout-chan (async/timeout 500)
        batch (->> (range batch-size)
                   (map (fn [_]
                          (let [result (first (async/alts!! [channel timeout-chan] :priority true))]
                            result)))
                   (remove (comp nil?)))]
    (if (drained? (last batch))
      [(filter #(not (contains? % :drained)) batch) true]
      [batch false])))
