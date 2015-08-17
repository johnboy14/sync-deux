(ns govtrack-sync-clj.utils.cypher-utils
  (:require [clojurewerkz.neocons.rest.transaction :as tx]
            [clojure.tools.logging :as log]))

(defn with-tx [connection statements]
  (let [transaction (tx/begin-tx connection)]
   (tx/with-transaction
     connection
     transaction
     true
     (let [[_ [r]] (tx/execute
                     connection
                     transaction
                     statements)]
       (log/info (:data r))))))