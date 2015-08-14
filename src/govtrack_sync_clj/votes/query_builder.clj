(ns govtrack-sync-clj.votes.query-builder)

(defn construct-vote-query [vote-detail]
  (let [vote-id (get-in vote-detail [:vote_id])]
    (str "MERGE (v:Vote {vote_id: '" vote-id "'}) ON CREATE SET v = {props} ON MATCH SET v = {props}")))

(defn construct-bill-query [vote-detail]
  (let [bill-id (get-in vote-detail [:bill_id])]
    (str "MERGE (b:Bill {bill_id: '" bill-id "'}) MERGE b-[bv:BillVote]->v")))

(defn construct-votes-query [votes type]
  (if-not (empty? votes)
    (let [lis-ids (map #(get-in % [:id]) votes)]
     (reduce str (map #(str " MERGE (la" % ":Legislator {lis: '" % "'}) MERGE la" % "-[vr" % ":voteon {vote: '" (name type) "'}]->v ") lis-ids)))))

(defn construct-vote-merge-query [vote-detail votes]
  (str (construct-vote-query vote-detail) " " (construct-bill-query vote-detail) " "
       (construct-votes-query (:Yea votes) :yea) " "
       (construct-votes-query (:Nea votes) :nea) " "
       (construct-votes-query ((keyword "Not Voting") votes) :notvoting) " "
       (construct-votes-query (:Present votes) :present) " "))