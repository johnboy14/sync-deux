(ns govtrack-sync-clj.utils.chan-utils
  (:gen-class)
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [govtrack-sync-clj.utils.file-utils :as utils]))

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

(def drained-message "{\"drained\":\"true\"}")

(defn slurp-files-onto-chan [dir chan promise]
  (log/info "Reading Files Directory " dir)
  (async/go
    (doseq [file (utils/walk dir (re-pattern ".*data.json"))]
      (async/>!! chan (slurp file)))
    (log/info (str "Finished Reading Files Directory " dir))
    (async/>!! chan drained-message)
    (deliver promise true)))
