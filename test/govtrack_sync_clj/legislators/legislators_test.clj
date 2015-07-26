(ns govtrack-sync-clj.legislators.legislators-test
  (:use midje.sweet)
  (:require [clojurewerkz.elastisch.rest :as esr]
    [clojurewerkz.elastisch.rest.index :as esi]
    [clojurewerkz.elastisch.rest.document :as esd]
    [clojurewerkz.neocons.rest :as nr]
    [govtrack-sync-clj.legislators.legislators :as leg]))

(def es-config {:url "http://localhost:9200" :indexes ["congress"]
                :neo-url "http://localhost:7474/db/data" :neo-username "neo4j" :neo-password "password"})

(defn clean-es [config]
  (let [connection (esr/connect (:url config))]
    (doseq [index (:indexes config)]
      (esi/delete connection index))))

(defn create-es [config]
  (let [connection (esr/connect (:url config))]
    (doseq [index (:indexes config)]
      (esi/create connection index))))

(facts "A Suite of tests for parsing legislator data from a .yaml file, this data is then synced to
        elasticsearch"
       (against-background [(before :facts (do (clean-es es-config)
                                               (create-es es-config)))]
                           (fact "Given a .yaml file containing two govtrack_sync_clj.legislators, then parse and persist to elasticsearch"
                                 (let [connection (esr/connect (:url es-config))
                                       _ (leg/persist-legislators-es "test-resources/legislators/legislators-current.yaml" connection "congress" "legislator")
                                       legislator (esd/get connection "congress" "legislator" "00136")]
                                   (:_source legislator) => (contains {:thomas "00136"
                                                                       :bioguide "B000944"
                                                                       :lis "S307"
                                                                       :govtrack "400050"
                                                                       :opensecrets "N00003535"
                                                                       :votesmart "27018"
                                                                       :cspan "5051"
                                                                       :wikipedia "Sherrod Brown"
                                                                       :ballotpedia "Sherrod Brown"
                                                                       :maplight "168"
                                                                       :washington_post "gIQA3O2w9O"
                                                                       :icpsr "29389"
                                                                       :first_name "Sherrod"
                                                                       :last_name "Brown"
                                                                       :birthday "1952-11-09"
                                                                       :gender "M"
                                                                       :religion "Lutheran"
                                                                       :type "sen"
                                                                       :state "OH"
                                                                       :party "Democrat"
                                                                       :class "1"
                                                                       :url "http://www.brown.senate.gov"
                                                                       :address "713 Hart Senate Office Building Washington DC 20510"
                                                                       :phone "202-224-2315"
                                                                       :fax "202-228-6321"
                                                                       :state_rank "senior"
                                                                       :rss_url "http://www.brown.senate.gov/rss/feeds/?type=all&amp;"
                                                                       :start "2013-01-03"
                                                                       :end  "2019-01-03"})))

                           (fact "Given a legislator .yaml file location, index and type, then persist records to database"
                                 (let [_ (leg/persist-legislators es-config "test-resources/legislators/legislators-current.yaml" "congress" "legislator")
                                       legislator (esd/get (esr/connect (:url es-config)) "congress" "legislator" "00136")]
                                   (:_source legislator) => (contains {:thomas "00136"
                                                                       :bioguide "B000944"
                                                                       :lis "S307"
                                                                       :govtrack "400050"
                                                                       :opensecrets "N00003535"
                                                                       :votesmart "27018"
                                                                       :cspan "5051"
                                                                       :wikipedia "Sherrod Brown"
                                                                       :ballotpedia "Sherrod Brown"
                                                                       :maplight "168"
                                                                       :washington_post "gIQA3O2w9O"
                                                                       :icpsr "29389"
                                                                       :first_name "Sherrod"
                                                                       :last_name "Brown"
                                                                       :birthday "1952-11-09"
                                                                       :gender "M"
                                                                       :religion "Lutheran"
                                                                       :type "sen"
                                                                       :state "OH"
                                                                       :party "Democrat"
                                                                       :class "1"
                                                                       :url "http://www.brown.senate.gov"
                                                                       :address "713 Hart Senate Office Building Washington DC 20510"
                                                                       :phone "202-224-2315"
                                                                       :fax "202-228-6321"
                                                                       :state_rank "senior"
                                                                       :rss_url "http://www.brown.senate.gov/rss/feeds/?type=all&amp;"
                                                                       :start "2013-01-03"
                                                                       :end  "2019-01-03"})))))

(facts "A Suite of tests for parsing legislator data from a .yaml file, this data is then indexed into Neo4J"
       (fact "Given a legislator.yaml file location, add the legislators to Neo4J"
             (let [connection (nr/connect (:neo-url es-config) (:neo-username es-config) (:neo-password es-config))]
               (leg/persist-legislators-neo "test-resources/legislators/legislators-current.yaml" connection))))


