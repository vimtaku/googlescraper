(ns googlescraper.core
  (:gen-class)
  (:use [clojure.java.io])
  (:require
    [net.cgrand.enlive-html :as e]
    [clj-http.client :as client]
    [clojure.data.json :as json]
  )
)

(def google-search-uri "https://www.google.co.jp/search")
(defn add-query-param [url & param]
  (str url "?q=" param)
)

(defn get-by-header [url params]
  (client/get url
    {
     :query-params params
     :headers
     {
      :Accept "text/html, application/xhtml+xml,application/xml, text/javascript, */*; q=0.01"
      :Accept-Encoding "gzip, deflate"
      :Accept-Language "ja,en-us;q=0.7,en;q=0.3"
      :User-Agent "googlescraper"
     }})
)

(defn google-by-keyword-scrape-result [k start]
  (e/html-resource
    (java.io.StringReader.
      ((get-by-header (add-query-param google-search-uri) {:q k :start start}) :body)
    )
  )
)

(defn google-by-keyword-scrape-result-with-sleep [k start ms]
  (Thread/sleep ms)
  (google-by-keyword-scrape-result k start)
)

(defn sleeptime []
  (+ 2000 (rand 3000))
)

(defn get-sites-by-keyword [k start]
  (let [a-tags (e/select
                 (google-by-keyword-scrape-result-with-sleep
                   k start sleeptime
                 [:h3 :a]
               )
        a-list (map #(get-in % [:attrs :href]) a-tags)
        uri-matches (map #(re-seq #"url\?q=(https?://.*)&sa=U" %) a-list)
       ]
       (filter #(not (nil? %)) (map #(-> % first fnext) uri-matches))
  )
)

(defn search-google [k host start till]
  (defn inner-search-google [li n]
    (cond (empty? li) -1
          (re-find (re-pattern host) (first li)) n
          :else (inner-search-google (next li) (+ n 1))
    )
  )
  ;; (println "searching...")
  ;; (println start)
  (let [
        result (get-sites-by-keyword k start)
        rank (inner-search-google result start)
       ]
    (if (= rank -1)
        (if (<= (+ 10 start) till)
            (search-google k host (+ 10 start) till)
            -1
        )
        rank
    )
  )
)
(defn get-keyword-rank
  ([k host] (inc (search-google k host 0 50)))
  ([k host till] (inc (search-google k host 0 till)))
)


(defn -main [& args]
  (println (apply get-keyword-rank args))
)
