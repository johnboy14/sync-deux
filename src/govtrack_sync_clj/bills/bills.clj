(ns govtrack-sync-clj.bills.bills
  (:require [clojurewerkz.elastisch.rest :as nsr]
            [clojurewerkz.elastisch.rest.document]
            [clojure.core.async :as async]))

(defn read-bills-onto-chan [dir chan])

(defn persist-bill-to-es [connection chan])

(defn persist-bills [config]
  (let [connection (nsr/connect (:url config))
        es-chan (async/chan 100)]
    (read-bills-onto-chan (:bill-dir config) es-chan)
    (persist-bill-to-es connection es-chan)))
