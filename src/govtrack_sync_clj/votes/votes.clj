(ns govtrack-sync-clj.votes.votes
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.cypher :as cy]
            [govtrack-sync-clj.votes.transformers :as transformers]
            [govtrack-sync-clj.utils.chan-utils :as chan-utils]
            [govtrack-sync-clj.votes.query-builder :as builder]))

(defn- persist-votes-to-neo [connection chan promise]
  (async/go-loop []
    (let [[batch drained?] (chan-utils/batch chan 500)]
      (if-not (empty? batch)
        (doseq [vote batch]
          (cy/query connection (builder/construct-vote-merge-query (:vote-details vote) (:votes vote)) {:props (:vote-details vote)})))
      (if (false? drained?)
        (recur)
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