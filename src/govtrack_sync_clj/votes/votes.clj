(ns govtrack-sync-clj.votes.votes
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojurewerkz.neocons.rest :as nr]
            [govtrack-sync-clj.votes.transformers :as transformers]
            [govtrack-sync-clj.utils.chan-utils :as chan-utils]
            [govtrack-sync-clj.votes.query-builder :as builder]
            [govtrack-sync-clj.utils.cypher-utils :as transaction-utils]))

(defn- persist-votes-to-neo [connection chan promise]
  (loop []
    (let [[batch drained?] (chan-utils/batch chan 2)]
      (log/info (str "Uploading " (count batch) " Votes to Neo4J"))
      (if-not (empty? batch)
        (transaction-utils/with-tx connection (builder/construct-votes-merge-transaction-query batch)))
      (if (false? drained?)
        (do (log/info (str "Finished writing " (count batch) " votes to Neo4J"))
            (recur))
        (do (log/info "Finished uploading Votes to Neo4J")
            (deliver promise true))))))

(defn persist-votes [config]
  (log/info "Starting Vote Sync Job")
  (let [connection (nr/connect (:neo-url config) (:neo-username config) (:neo-password config))
        neo-vote-file-chan (async/chan 100 transformers/vote-neo-transformer)
        read-votes-promise (promise)
        finished-neo-sync (promise)]
    (chan-utils/slurp-files-onto-chan (:vote-dir config) neo-vote-file-chan read-votes-promise)
    (persist-votes-to-neo connection neo-vote-file-chan finished-neo-sync)
    @read-votes-promise
    @finished-neo-sync
    (log/info "Finished Vote Sync Job")))