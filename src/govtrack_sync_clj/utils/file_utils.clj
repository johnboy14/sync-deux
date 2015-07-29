(ns govtrack-sync-clj.utils.file-utils
  (:require [clojure.walk :as w]
            [clojure.java.io :as io]))

(defn walk [dirpath pattern]
  (doall (filter #(re-matches pattern (.getName %))
                 (file-seq (io/file dirpath)))))
