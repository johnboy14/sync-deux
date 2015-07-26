(ns govtrack-sync-clj.legislators.legislators
  (:require [clj-yaml.core :as yaml]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.bulk :as esb]
            [clojurewerkz.neocons.rest.transaction   :as tx]
            [cheshire.core :as ch]))

(defn parse-legislator [legislator]
  (let [id (:id legislator)
        name (:name legislator)
        bio (:bio legislator)
        current_term (last (:terms legislator))] 
    (-> {}
       (assoc :_id (:thomas id))
       (assoc :thomas (:thomas id))
       (assoc :bioguide (:bioguide id))
       (assoc :govtrack (str (:govtrack id)))
       (assoc :lis (:lis id))
       (assoc :opensecrets (:opensecrets id))
       (assoc :votesmart (str (:votesmart id)))
       (assoc :cspan (str (:cspan id)))
       (assoc :wikipedia (:wikipedia id))
       (assoc :ballotpedia (:ballotpedia id))
       (assoc :maplight (str (:maplight id)))
       (assoc :washington_post (:washington_post id))
       (assoc :icpsr (str (:icpsr id)))
       (assoc :first_name (:first name))
       (assoc :last_name (:last name))
       (assoc :birthday (:birthday bio))
       (assoc :gender (:gender bio))
       (assoc :religion (:religion bio))
       (assoc :type (:type current_term))
       (assoc :start (:start current_term))
       (assoc :end (:end current_term))
       (assoc :state (:state current_term))
       (assoc :party (:party current_term))
       (assoc :class (str (:class current_term)))
       (assoc :url (:url current_term))
       (assoc :address (:address current_term))
       (assoc :phone (:phone current_term))
       (assoc :fax (:fax current_term))
       (assoc :state_rank (:state_rank current_term))
       (assoc :rss_url (:rss_url current_term))
       )))

(defn prepare-map-for-merge [m]
  (reduce str (butlast (butlast (reduce str (map (fn [[k v]] (str (reduce str (rest (str (str k) ": "))) "'" v "', ")) m))))))

(defn parse-legislators [legislators]
  (->> legislators
       (map parse-legislator)))

(defn create-neo4j-statement [legislator]
  (tx/statement (str "MERGE (l:Legislator {" (prepare-map-for-merge legislator) "}) return l")))

(defn persist-legislators-es [file connection index type]
  (let [legislators (yaml/parse-string (slurp (java.io.File. file)))
        parsed-legislators (parse-legislators legislators)]
    (esb/bulk-with-index-and-type connection index (str type) (esb/bulk-index parsed-legislators))))

(defn construct-neo4j-legislators [file]
  (let [legislators (yaml/parse-string (slurp (java.io.File. file)))
        parsed-legislators (parse-legislators legislators)
        neo4j-statements (map create-neo4j-statement parsed-legislators)]
    neo4j-statements))

(defn persist-legislators-neo [file connection]
  (let [transaction (tx/begin-tx connection)]
    (tx/with-transaction connection transaction true
                         (let [[_ result] (tx/execute
                                            connection
                                            transaction
                                            (construct-neo4j-legislators file))]))))

(defn persist-legislators [config file-loc index type]
  (let [connection (esr/connect (:url config))]
    (persist-legislators-es file-loc connection index type)))
