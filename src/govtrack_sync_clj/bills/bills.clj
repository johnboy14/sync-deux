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
            [govtrack-sync-clj.bills.transformers :as transformer]
            [govtrack-sync-clj.bills.query-builder :as builder]
            [clojurewerkz.neocons.rest.cypher :as cy]))

(defn- persist-bill-to-es [connection index type chan promise]
  (es-client/write-to-es-from-chan connection index type chan promise))

(defn- persist-bill-to-neo [connection chan promise]
  (async/go-loop []
    (let [[batch drained?] (chan-utils/batch chan 10)]
      (log/info (str "Uploading " (count batch) " bills to Neo4J"))
      (if-not (empty? batch)
        (doseq [bill batch]
          (let [bill-details (:bill-details bill)
                cosponsors (:cosponsors bill)]
            (cy/query connection (builder/construct-bill-merge-query bill-details cosponsors) {:props bill-details}))))
      (if (false? drained?)
        (do (log/info (str "Finished writing " (count batch) " bills to Neo4J"))
            (recur))
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
