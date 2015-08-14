(ns govtrack-sync-clj.bills.bill-query-builder-test
  (:use midje.sweet)
  (:require [cheshire.core :as ch]))

(defn bill-merge-query [bill]
  (let [bill-id (get-in bill [:bill_id])]
    (str "MERGE (b:Bill {bill_id: '"bill-id"'}) ON CREATE SET b = {props} ON MATCH SET b = {props}")))

(defn bill-sponsor-query [bill]
  (let [thomas (get-in (get-in bill [:sponsor]) [:thomas_id])]
    (str "MERGE (s:Sponsor {thomas: '" thomas "'}) MERGE b-[sb:sponsoredby]->s")))

(defn bill-cosponsor-query [bill]
  (let [cosponsors (get-in bill [:cosponsors])
        thomas-ids (map #(get-in % [:thomas_id]) cosponsors)]
    (reduce str (map #(str "MERGE (A" % ":Sponsor {thomas: '" % "'}) MERGE A" % "-[B" % "csr:cosponsoring]->b ") thomas-ids))))

(defn bill-subject-query [bill]
  (let [subject (get-in bill [:subjects_top_term])]
    (str "MERGE (sub:Subject {name: '" subject "'}) MERGE sub-[subr:hasSubjectTerm]->b")))

(def bill (ch/parse-string (slurp "test-resources/bills/s/s295/data.json") true))
(fact ""
      (str (bill-merge-query bill) " " (bill-sponsor-query bill) " " (bill-subject-query bill) " " (bill-cosponsor-query bill)))
