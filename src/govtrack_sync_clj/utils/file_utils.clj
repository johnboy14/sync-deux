(ns govtrack-sync-clj.utils.file-utils
  (:gen-class)
  (:require [clojure.walk :as w]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.cypher :as cy]
            [clojure.java.io :as io]))

(defn get-node [connection id]
  (if (nil? id)
    nil
    (nn/get connection id)))

(defn retrieve-id [connection query]
  (let [data (:data (cy/query connection query))]
    (if (empty? data)
      nil
      (long (first (first data))))))

(defn walk [dirpath pattern]
  (doall (filter #(re-matches pattern (.getName %))
                 (file-seq (io/file dirpath)))))
