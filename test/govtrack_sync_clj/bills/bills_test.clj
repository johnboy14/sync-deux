(ns govtrack-sync-clj.bills.bills-test
  (:use midje.sweet)
  (:require [govtrack-sync-clj.utils.test-utils :as utils]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.cypher :as cy]
            [govtrack-sync-clj.bills.bills :as bills]
            [govtrack-sync-clj.legislators.legislators :as leg]))

(against-background [(before :facts (utils/teardown-setup))]
                    (facts "Following Tests cover the creation of bills and corresponding relationships
                            between a bill and its sponsors and cosponsors"

                           (fact "Given a directory containing bills, sync the bills to elasticsearch"
                                 (leg/persist-legislators utils/es-config "test-resources/legislators/legislators-current.yaml" "congress" "legislator")
                                 (bills/persist-bills utils/es-config)
                                 (let [connection (esr/connect (:url utils/es-config))
                                       bill1 (esd/get connection "congress" "bill" "s1787-114")
                                       bill2 (esd/get connection "congress" "bill" "s890-114")]
                                   (:_source bill1) => (contains {:bill_id "s1787-114"})
                                   (:_source bill2) => (contains {:bill_id "s890-114"})))

                           (fact "Given a directory containing bills, sync the bills to Neo4J"
                                 (leg/persist-legislators utils/es-config "test-resources/legislators/legislators-current.yaml" "congress" "legislator")
                                 (bills/persist-bills utils/es-config)
                                 (let [connection (nr/connect (:neo-url utils/es-config) (:neo-username utils/es-config) (:neo-password utils/es-config))
                                       {:keys [data columns]} (cy/query connection "MATCH (l:Bill) RETURN l.bill_id,l.sponsor,l.cosponsors")]
                                   (count data) => 2
                                   data => (contains [["s1787-114" "00136" "01983"]
                                                      ["s890-114" "00172" "01558,01965,02076,02194,00116,00174,01828,01984,01332,01969,01937,01844,01983,01900,01837,01929,01010,01901,01531,01829,01567,02182,01823,01247"]]
                                                     :in-any-order)))
                           (fact "Given a directory containing bills, make a relationship between the bill and its sponsor"
                                 (leg/persist-legislators utils/es-config "test-resources/legislators/legislators-current.yaml" "congress" "legislator")
                                 (bills/persist-bills utils/es-config)
                                 (bills/persist-bills utils/es-config)
                                 (let [connection (nr/connect (:neo-url utils/es-config) (:neo-username utils/es-config) (:neo-password utils/es-config))
                                       bill (:data (cy/query connection "MATCH (l:Bill {bill_id: 's890-114'}) RETURN l.bill_id"))
                                       sponsoredby-relationship (:data (cy/query connection "MATCH (b:Bill {bill_id: 's890-114'})-[r:sponsoredby]->(l:Legislator) RETURN l"))
                                       sponsoring-relationship (:data (cy/query connection "MATCH (b:Bill {bill_id: 's890-114'})<-[r:sponsoring]-(l:Legislator) RETURN l"))
                                       cosponsoring-relationship (:data (cy/query connection "MATCH (b:Bill {bill_id: 's890-114'})<-[r:cosponsoring]-(l:Legislator) RETURN l"))
                                       cosponsoredby-relationship (:data (cy/query connection "MATCH (b:Bill {bill_id: 's890-114'})-[r:cosponsoredby]->(l:Legislator) RETURN l"))]
                                   (count bill) => 1
                                   (count sponsoredby-relationship) => 1
                                   (count sponsoring-relationship) => 1
                                   (count cosponsoring-relationship) => 3
                                   (count cosponsoredby-relationship) => 3))))

