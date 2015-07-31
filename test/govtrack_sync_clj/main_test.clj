(ns govtrack-sync-clj.main-test
  (:use [midje.sweet])
  (:require [govtrack-sync-clj.utils.test-utils :as utils]
            [govtrack-sync-clj.main :as main]))


;(facts "Integration tests to ensure full suite of Jobs works as expected"
;       (against-background [(before :facts (utils/teardown-setup))]
;                           (fact "Test full integration"
;                                 (let [_ (main/-main utils/es-config)]
;                                   1 => 1))))
