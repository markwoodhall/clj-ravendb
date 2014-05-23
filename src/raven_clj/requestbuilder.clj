(ns raven-clj.requestbuilder
  (:require [clj-http.client :as client])
  (:require [clojure.data.json :as json]))


(defn- get-url
  [url database, endpoint]
  (let [fragments (list url "Databases" database endpoint)]
    (clojure.string/join "/" fragments)))

(defn build-load-request
  [url database doc-ids]
  (let [request-url (get-url url database "Queries")]
    {
     :url request-url
     :body (json/write-str doc-ids)
     }))

(defn build-cmd-request
  [url database cmds]
  (let [request-url (get-url url database "bulk_docs")]
    {
     :url request-url
     :body (json/write-str cmds)
     }))

(defn build-putidx-request
  [url database idx]
  (let [request-url (str (get-url url database "indexes/") (idx :name))]
    {
     :url request-url
     :body (json/write-str {:Map (str 
                                   "from " (idx :alias) " in docs"
                                   " where " (idx :where) 
                                   " select " (idx :select))})
     }))

(defn build-qryidx-request
  [url database qry]
  (let [request-url (str (get-url url database "indexes/") (qry :index) "?query=")
        criteria (clojure.string/join " AND " (into []
                                                (for [[k v] (dissoc qry :index)]
                                                  (str (name k) ":" v))))]
    {
     :url (str request-url criteria)
     }))