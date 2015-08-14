(ns govtrack-sync-clj.bills.query-builder)

(defn bill-merge-query [bill]
  (let [bill-id (get-in bill [:bill_id])]
    (str "MERGE (b:Bill {bill_id: '"bill-id"'}) ON CREATE SET b = {props} ON MATCH SET b = {props}")))

(defn bill-sponsor-query [bill]
  (let [thomas (get-in bill [:sponsor])]
    (str "MERGE (s:Legislator {thomas: '" thomas "'}) MERGE b-[sb:sponsoredby]->s MERGE s-[sr:sponsoring]->b")))

(defn bill-cosponsor-query [cosponsors]
  (let [thomas-ids (map #(get-in % [:thomas_id]) cosponsors)]
    (reduce str (map #(str "MERGE (A" % ":Legislator {thomas: '" % "'}) MERGE A" % "-[B" % "csr:cosponsoring]->b MERGE b-[C" % "csb:cosponsoredby]->A" % " ") thomas-ids))))

(defn bill-subject-query [bill]
  (let [subject (get-in bill [:subjects_top_term])]
    (if-not (nil? subject)
      (str "MERGE (sub:Subject {name: '" subject "'}) MERGE sub-[subr:hasSubjectTerm]->b"))))

(defn construct-bill-merge-query [bill-details cosponsors]
  (str (bill-merge-query bill-details) " " (bill-sponsor-query bill-details) " " (bill-subject-query bill-details) " " (bill-cosponsor-query cosponsors)))