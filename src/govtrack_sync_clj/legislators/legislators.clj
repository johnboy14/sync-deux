(ns govtrack-sync-clj.legislators.legislators
  (:require [clj-yaml.core :as yaml]
            [clojurewerkz.elastisch.rest.document :as esd]
            [clojurewerkz.elastisch.rest.bulk :as esb]))

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


;- type: sen
;start: '2013-01-03'
;end: '2019-01-03'
;state: OH
;party: Democrat
;class: 1
;url: http://www.brown.senate.gov
;address: 713 Hart Senate Office Building Washington DC 20510
;phone: 202-224-2315
;fax: 202-228-6321
;contact_form: http://www.brown.senate.gov/contact
;office: 713 Hart Senate Office Building
;state_rank: senior
;rss_url: http://www.brown.senate.gov/rss/feeds/?type=all&amp;

(defn parse-legislators [legislators]
  (->> legislators
       (map parse-legislator)))

(defn persist-legislators-es [file connection index type]
  (let [legislators (yaml/parse-string (slurp (java.io.File. file)))
        parsed-legislators (parse-legislators legislators)]
    (esb/bulk-with-index-and-type connection index (str type) (esb/bulk-index parsed-legislators))))
