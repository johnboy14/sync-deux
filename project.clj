(defproject govtrack-sync-clj "0.0.1-SNAPSHOT"
  :description "Sync data about Congress into Elastic Search and Neo4J Databases"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-yaml "0.4.0"]
                 [clojurewerkz/elastisch "2.1.0"]
                 [clojurewerkz/neocons "3.1.0-beta3"]
                 [cheshire "5.5.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.0.2"]
                 [org.apache.logging.log4j/log4j-core "2.0.2"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]]
  :profiles {:dev {:dependencies [[midje "1.7.0"]]}
             :uberjar {:aot :all}}
  :main ^:skip-aot govtrack-sync-clj.main
  :target-path "target/%s"
  :plugins [[lein-midje "3.1.3"]])
