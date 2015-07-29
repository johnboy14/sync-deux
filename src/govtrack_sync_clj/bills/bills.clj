(ns govtrack-sync-clj.bills.bills
  (:require [clojurewerkz.elastisch.rest :as nsr]
            [clojurewerkz.elastisch.rest.document]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.relationships :as nnr]
            [clojurewerkz.neocons.rest.cypher :as cy]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [govtrack-sync-clj.elasticsearch :as es-client]
            [govtrack-sync-clj.utils.file-utils :as utils]
            [govtrack-sync-clj.utils.chan-utils :as chan-utils]
            [govtrack-sync-clj.bills.transformers :as transformer]))

(def drained-message "{\"drained\":\"true\"}")

(defn retrieve-id [connection query]
  (let [data (:data (cy/query connection query))]
    (if (empty? data)
      nil
      (long (first (first data))))))

(defn retrieve-bill-id [connection id]
  (let [data (:data (cy/query connection (str "MATCH (b:Bill {bill_id: '" id "'}) return id(b)")))]
    (if (empty? data)
      nil
      (long (first (first data))))))

(defn read-bills-onto-chan [dir chan promise]
  (log/info "Reading Bills Directory " dir)
  (async/go
    (doseq [file (utils/walk dir (re-pattern ".*data.json"))]
      (async/>!! chan (slurp file)))
    (log/info (str "Finished Reading Bills Directory " dir))
    (async/>!! chan drained-message)
    (deliver promise true)))

(defn persist-bill-to-es [connection index type chan promise]
  (es-client/write-to-es connection index type chan promise))

(defn create-bill-sponsor-rel [connection from sponsor]
  (let [existing-rel (nnr/all-for connection from)
        sponsor-id (retrieve-id connection (str "MATCH (l:Legislator {thomas: '" sponsor "'}) return id(l)"))
        exisiting-sponsor (nn/get connection sponsor-id)]
    (println existing-rel)
    (if (and (empty? existing-rel) (not (nil? exisiting-sponsor)))
      (do (nnr/create connection from exisiting-sponsor "sponsoredby")
          (nnr/create connection exisiting-sponsor from "sponsoring")))))

(defn persist-bill-to-neo [connection chan promise]
  (async/go-loop []
    (let [[batch drained?] (chan-utils/batch chan 500)]
      (if-not (empty? batch)
        (doseq [bill batch]
          (let [existing-id (retrieve-bill-id connection (:bill_id bill))]
            (if (nil? existing-id)
              (let [bill-node (nn/create connection bill)]
                (nl/add connection bill-node "Bill")
                (create-bill-sponsor-rel connection bill-node (:sponsor bill)))
              (nn/update connection existing-id bill)))))
      (if (false? drained?)
        (recur)
        (do (log/info "Finished uploading Bills to Neo4J")
            (deliver promise true))))))

(defn persist-bills [config]
  (log/info "Starting Bill Sync Job")
  (let [connection (nsr/connect (:url config))
        neo-connection (nr/connect (:neo-url config) (:neo-username config) (:neo-password config))
        bill-chan (async/chan 100 transformer/bill-transformer)
        es-chan (async/chan 100)
        neo-chan (async/chan 100 transformer/bill-neo-transformer)
        read-bill-promise (promise)
        finished-es-sync (promise)
        finished-neo-sync (promise)
        mult (async/mult bill-chan)
        _ (async/tap mult es-chan)
        _ (async/tap mult neo-chan)]
    (read-bills-onto-chan (:bill-dir config) bill-chan read-bill-promise)
    (persist-bill-to-es connection (:es-index config) (:es-bill-type config) es-chan finished-es-sync)
    (persist-bill-to-neo neo-connection neo-chan finished-neo-sync)
    @read-bill-promise
    @finished-es-sync
    @finished-neo-sync)
  (log/info "Stopping Bill Sync Job"))
