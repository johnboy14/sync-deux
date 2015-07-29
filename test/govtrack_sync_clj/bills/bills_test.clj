(ns govtrack-sync-clj.bills.bills-test
  (:use midje.sweet)
  (:require [govtrack-sync-clj.utils.test-utils :as utils]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [govtrack-sync-clj.bills.bills :as bills]))

(against-background [(before :facts (utils/teardown-setup))]
                    (facts "Following Tests cover the creation of bills and corresponding relationships
                            between a bill and its sponsors and cosponsors"

                           ;(fact "Given a director containing bills, sync the bills to elasticsearch"
                           ;      (bills/persist-bills utils/es-config)
                           ;      (let [connection (esr/connect (:url utils/es-config))
                           ;            bill (esd/get connection "congress" "bill" "s1787-114")]
                           ;        (:_source bill) => (contains {:bill_id "s1787-114"})))
                           ))
