(ns govtrack-sync-clj.utils.test-utils
  (:require [clojurewerkz.elastisch.rest.index :as esi]
            [clojurewerkz.neocons.rest.constraints :as nrc]
            [clojurewerkz.elastisch.rest :as esr]
            [clojurewerkz.neocons.rest :as nr]
            [clojurewerkz.neocons.rest.cypher :as cy]))

(def es-config {:url "http://localhost:9200" :indexes ["congress"]
                :neo-url "http://localhost:7474/db/data" :neo-username "neo4j" :neo-password "password"
                :bill-dir "test-resources/bills" :es-index "congress" :es-bill-type "bill"})

(defn clean-es [config]
  (let [connection (esr/connect (:url config))]
    (doseq [index (:indexes config)]
      (esi/delete connection index))))

(defn create-es [config]
  (let [connection (esr/connect (:url config))]
    (doseq [index (:indexes config)]
      (esi/create connection index))))

(defn clear-all-nodes [config]
  (let [connection (nr/connect (:neo-url config) (:neo-username config) (:neo-password config))]
    (cy/query connection "MATCH n DELETE n")))

(defn clear-all-relationships [config]
  (let [connection (nr/connect (:neo-url config) (:neo-username config) (:neo-password config))]
    (cy/query connection "START r=relationship(*) DELETE r")))

(defn clear-constraints [config]
  (let [connection (nr/connect (:neo-url config) (:neo-username config) (:neo-password config))]
    (try
      (nrc/drop-unique connection "Legislator" "thomas")
      (nrc/drop-unique connection "Bill" "bill_id")
      (catch Exception e (println e)))))

(defn create-constraints [config]
  (let [connection (nr/connect (:neo-url config) (:neo-username config) (:neo-password config))]
    (try
      (nrc/create-unique connection "Legislator" "thomas")
      (nrc/create-unique connection "Bill" "bill_id")
      (catch Exception e (println e)))))

(defn teardown-setup []
  (clean-es es-config)
  (create-es es-config)
  (clear-all-relationships es-config)
  (clear-all-nodes es-config)
  (clear-constraints es-config)
  (create-constraints es-config))