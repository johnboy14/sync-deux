(ns govtrack-sync-clj.elasticsearch.elasticsearch
  (:gen-class)
  (:require [clojurewerkz.elastisch.rest.bulk :as esrb]
            [clojure.tools.logging :as log]
            [clojure.core.async :as async]
            [govtrack-sync-clj.utils.chan-utils :as chan-utils]))

(defn write-to-es-from-chan [connection index type channel promise]
  (async/go-loop []
    (let [[batch drained?] (chan-utils/batch channel 500)]
      (if-not (empty? batch)
        (esrb/bulk-with-index-and-type connection index (str type) (esrb/bulk-index batch)))
      (log/info (str (count batch) " Documents written to ElasticSearch"))
      (if (false? drained?)
        (recur)
        (do (log/info (str "Finished writing " type "'s to elasticsearch"))
            (deliver promise true))))))
