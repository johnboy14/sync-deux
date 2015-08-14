(ns govtrack-sync-clj.legislators.query-builder)

(defn construct-legislator-merge-query [legislator]
  (str "MERGE (l:Legislator {thomas: '" (:thomas legislator) "'}) ON CREATE SET l = {props} ON MATCH SET l = {props}"))