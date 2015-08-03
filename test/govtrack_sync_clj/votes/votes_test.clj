(ns govtrack-sync-clj.votes.votes-test
  (:use midje.sweet)
  (:require [govtrack-sync-clj.utils.test-utils :as utils]
            [govtrack-sync-clj.votes.votes :as votes]))

(against-background [(before :facts (utils/teardown-setup))]

                    (facts "Test cases for creating relationships between votes and bills and votes and legislators"
                           (fact "Given a directory of vote records, create vote record in Neo4J"
                                 (votes/persist-votes utils/es-config))))
