(ns govtrack-sync-clj.votes.votes-test
  (:use midje.sweet)
  (:require [govtrack-sync-clj.utils.test-utils :as utils]
            [govtrack-sync-clj.votes.votes :as votes]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.cypher :as cy]))

(against-background [(before :facts (utils/teardown-setup))]

                    (facts "Test cases for creating relationships between votes and bills and votes and legislators"
                           (fact "Given a directory of vote records, create vote record in Neo4J with the correct vote and bill_id"
                                 (votes/persist-votes utils/es-config)
                                 (let [connection (nr/connect (:neo-url utils/es-config) (:neo-username utils/es-config) (:neo-password utils/es-config))
                                       votes (:data (cy/query connection "MATCH (v:Vote {vote_id:'s55-114.2015'}) return v.vote_id, v.bill_id, v.nay, v.absent, v.yea"))
                                       voterecord (first votes)]
                                   (count votes) => 1
                                   voterecord => ["s55-114.2015" "s295-114" 0 2 98]))))
