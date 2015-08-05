(ns govtrack-sync-clj.votes.votes
  (:require [clojure.core.async :as async]
            [clojure.tools.logging :as log]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.relationships :as nnr]
            [clojurewerkz.neocons.rest.labels :as nl]
            [govtrack-sync-clj.votes.transformers :as transformers]
            [govtrack-sync-clj.utils.chan-utils :as chan-utils]
            [govtrack-sync-clj.utils.file-utils :as utils]))

(defn- create-voter-vote-rel-type [connection vote-node votes type]
  (doseq [voter votes]
    (let [sponsor-id (utils/retrieve-id connection (str "MATCH (l:Legislator {lis: '" (:id voter) "'}) return id(l)"))
          exisiting-sponsor (utils/get-node connection sponsor-id)]
      (if-not (nil? exisiting-sponsor)
        (nnr/maybe-create connection exisiting-sponsor vote-node "voteon" {:vote type})))))

(defn- create-voter-vote-rel [connection vote-node votes]
  (create-voter-vote-rel-type connection vote-node (:Nay votes) "nay")
  (create-voter-vote-rel-type connection vote-node (:Yea votes) "yea")
  (create-voter-vote-rel-type connection vote-node ((keyword "Not Voting") votes) "not-voting"))

(defn- create-bill-vote-rel [connection vote-node bill_id]
  (let [bill-id (utils/retrieve-id connection (str "MATCH (b:Bill {bill_id: '" bill_id "'}) return id(b)"))
        existing-bill (utils/get-node connection bill-id)]
    (if-not (nil? existing-bill)
      (nnr/maybe-create connection existing-bill vote-node "BillVote"))))

(defn- persist-votes-to-neo [connection chan promise]
  (async/go-loop []
    (let [[batch drained?] (chan-utils/batch chan 500)]
      (if-not (empty? batch)
        (doseq [vote batch]
          (let [vote-details (:vote-details vote)
                existing-id (utils/retrieve-id connection (str "MATCH (v:Vote {vote_id: '" (:vote_id vote-details) "'}) return id(v)"))]
            (if (nil? existing-id)
              (let [vote-node (nn/create connection vote-details)]
                (nl/add connection vote-node "Vote")
                (create-bill-vote-rel connection vote-node (:bill_id vote-details))
                (create-voter-vote-rel connection vote-node (:votes vote)))
              (nn/update connection existing-id vote-details)))))
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