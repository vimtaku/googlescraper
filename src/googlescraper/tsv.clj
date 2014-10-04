(ns googlescraper.tsv
  (:use [clojure.java.io])
  (:require
    [clojure-csv.core :as csv]
  )
)

(def path "./result.txt")

(defn write-result [s path]
  "Write s to path"
  (with-open [wrtr (writer path :append true)]
    (.write wrtr s)
  )
)

(defn write [& args]
  (write-result (csv/write-csv args :delimiter "\t") path)
)

