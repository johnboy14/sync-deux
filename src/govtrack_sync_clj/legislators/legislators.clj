(ns govtrack-sync-clj.legislators.legislators
  (:require [clj-yaml.core :as yaml]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.elastisch.rest.bulk :as esb]
            [clojurewerkz.neocons.rest.cypher :as cy]
            [clojurewerkz.neocons.rest.nodes :as nn]
            [clojurewerkz.neocons.rest.labels :as nl]
            [clojurewerkz.neocons.rest :as nr]
            [clojure.tools.logging :as log]))

(defn- parse-legislator [legislator]
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
       (assoc :rss_url (:rss_url current_term)))))

(defn- parse-legislators [legislators]
  (->> legislators
       (map parse-legislator)))

(defn- retrieve-existing-legislator-id [connection thomas-id]
  (let [data (:data (cy/query connection (str "MATCH (l:Legislator {thomas: '" thomas-id "'}) return id(l)")))]
    (if (empty? data)
      nil
      (long (first (first data))))))

(defn- persist-legislators-es [file connection index type]
  (let [legislators (yaml/parse-string (slurp (java.io.File. file)))
        parsed-legislators (parse-legislators legislators)]
    (esb/bulk-with-index-and-type connection index (str type) (esb/bulk-index parsed-legislators))))

(defn- construct-and-persist-neo4j-legislators [connection file]
  (let [legislators (yaml/parse-string (slurp (java.io.File. file)))
        parsed-legislators (parse-legislators legislators)]
    (doseq [legislator parsed-legislators]
      (let [existing-id (retrieve-existing-legislator-id connection (:thomas legislator))]
        (if (nil? existing-id)
          (nl/add connection (nn/create connection legislator) "Legislator")
          (nn/update connection existing-id legislator))))))

(defn- persist-legislators-neo [file connection]
  (construct-and-persist-neo4j-legislators connection file))

(defn persist-legislators [config file-loc index type]
  (log/info "Starting Legislator Task")
  (let [es-connection (esr/connect (:url config))
        neo-connection (nr/connect (:neo-url config) (:neo-username config) (:neo-password config))]
    (persist-legislators-es file-loc es-connection index type)
    (persist-legislators-neo file-loc neo-connection)))
