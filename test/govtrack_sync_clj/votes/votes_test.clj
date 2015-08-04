(ns govtrack-sync-clj.votes.votes-test
  (:use midje.sweet)
  (:require [govtrack-sync-clj.utils.test-utils :as utils]
            [govtrack-sync-clj.votes.votes :as votes]
            [govtrack-sync-clj.bills.bills :as bills]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.cypher :as cy]))

(against-background [(before :facts (utils/teardown-setup))]

                    (facts "Test cases for creating relationships between votes and bills and votes and legislators"
                           (fact "Given a directory of vote records, create vote record in Neo4J with the correct vote and bill_id"
                                 (bills/persist-bills utils/es-config)
                                 (votes/persist-votes utils/es-config)
                                 (let [connection (nr/connect (:neo-url utils/es-config) (:neo-username utils/es-config) (:neo-password utils/es-config))
                                       votes (:data (cy/query connection "MATCH (v:Vote {vote_id:'s55-114.2015'}) return v.vote_id, v.bill_id, v.nay, v.absent, v.yea"))
                                       voterecord (first votes)
                                       vote-bill-relationships (:data (cy/query connection "MATCH (b:Bill {bill_id:'s295-114'})-[r:BillVote]->(v:Vote) return v.vote_id, v.bill_id"))
                                       billVoteRelationship (first vote-bill-relationships)]
                                   (count votes) => 1
                                   voterecord => ["s55-114.2015" "s295-114" 0 2 98]
                                   (count vote-bill-relationships) => 1
                                   billVoteRelationship => ["s55-114.2015" "s295-114"]))))
