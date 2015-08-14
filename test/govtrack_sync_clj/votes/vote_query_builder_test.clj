(ns govtrack-sync-clj.votes.vote-query-builder-test
  (:use midje.sweet)
  (:require [cheshire.core :as ch]
            [govtrack-sync-clj.votes.query-builder :as b]
            [govtrack-sync-clj.votes.transformers :as t]))

(def vote (ch/parse-string (slurp "test-resources/votes/s55/data.json") true))

(fact ""
      (let [v (t/construct-neo-vote-payload vote)]
        ;(println (b/construct-vote-merge-query (:vote-details v) (:votes v)))
        ;(println (b/construct-votes-query ((keyword "Not Voting") (:votes v)) :notvoting))
        ))