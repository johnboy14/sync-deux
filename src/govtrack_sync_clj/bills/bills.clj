(ns govtrack-sync-clj.bills.bills
  (:gen-class)
  (:require [clojurewerkz.elastisch.rest :as nsr]
            [clojurewerkz.elastisch.rest.document]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.relationships :as nnr]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [govtrack-sync-clj.elasticsearch.elasticsearch :as es-client]
            [govtrack-sync-clj.utils.file-utils :as utils]
            [govtrack-sync-clj.utils.chan-utils :as chan-utils]
            [govtrack-sync-clj.bills.transformers :as transformer]))

(defn- persist-bill-to-es [connection index type chan promise]
  (es-client/write-to-es-from-chan connection index type chan promise))

(defn- create-bill-sponsor-rel [connection from sponsor]
  (let [sponsor-id (utils/retrieve-id connection (str "MATCH (l:Legislator {thomas: '" sponsor "'}) return id(l)"))
        exisiting-sponsor (utils/get-node connection sponsor-id)]
    (if-not (nil? exisiting-sponsor)
      (do (nnr/maybe-create connection from exisiting-sponsor "sponsoredby")
          (nnr/maybe-create connection exisiting-sponsor from "sponsoring")))))

(defn- create-bill-cosponsor-rel [connection from cosponsors]
  (if-not (nil? cosponsors)
    (doseq [cosponsor cosponsors]
      (let [sponsor-id (utils/retrieve-id connection (str "MATCH (l:Legislator {thomas: '" (:thomas_id cosponsor) "'}) return id(l)"))
            exisiting-sponsor (utils/get-node connection sponsor-id)]
        (if-not (nil? exisiting-sponsor)
          (do (nnr/maybe-create connection from exisiting-sponsor "cosponsoredby")
              (nnr/maybe-create connection exisiting-sponsor from "cosponsoring")))))))

(defn- persist-bill-to-neo [connection chan promise]
  (async/go-loop []
    (let [[batch drained?] (chan-utils/batch chan 500)]
      (if-not (empty? batch)
        (doseq [bill batch]
          (let [bill-details (:bill-details bill)
                cosponsors (:cosponsors bill)
                existing-id (utils/retrieve-id connection (str "MATCH (b:Bill {bill_id: '" (:bill_id bill-details) "'}) return id(b)"))]
            (if (nil? existing-id)
              (let [bill-node (nn/create connection bill-details)]
                (nl/add connection bill-node "Bill")
                (create-bill-sponsor-rel connection bill-node (:sponsor bill-details))
                (create-bill-cosponsor-rel connection bill-node cosponsors))
              (nn/update connection existing-id bill-details)))))
      (if (false? drained?)
        (recur)
        (do (log/info "Finished uploading Bills to Neo4J")
            (deliver promise true))))))

(defn persist-bills [config]
  (log/info "Starting Bill Sync Job")
  (let [connection (nsr/connect (:url config))
        neo-connection (nr/connect (:neo-url config) (:neo-username config) (:neo-password config))
        es-bill-file-chan (async/chan 100 transformer/es-bill-transformer)
        neo-bill-file-chan (async/chan 100 transformer/bill-neo-transformer)
        read-bill-promise (promise)
        finished-es-sync (promise)
        finished-neo-sync (promise)]
    (chan-utils/slurp-files-onto-chan (:bill-dir config) es-bill-file-chan read-bill-promise)
    (chan-utils/slurp-files-onto-chan (:bill-dir config) neo-bill-file-chan read-bill-promise)
    (persist-bill-to-es connection (:es-index config) (:es-bill-type config) es-bill-file-chan finished-es-sync)
    (persist-bill-to-neo neo-connection neo-bill-file-chan finished-neo-sync)
    @read-bill-promise
    @finished-es-sync
    @finished-neo-sync)
  (log/info "Stopping Bill Sync Job"))
