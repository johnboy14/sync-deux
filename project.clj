(defproject govtrack-sync-clj "0.0.1-SNAPSHOT"
  :description "Sync data about Congress into Elastic Search and Neo4J Databases"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-yaml "0.4.0"]
                 [clojurewerkz/elastisch "2.1.0"]
                 [clojurewerkz/neocons "3.1.0-beta3"]
                 [cheshire "5.5.0"]]
  :profiles {:dev {:dependencies [[midje "1.7.0"]]}})
  
