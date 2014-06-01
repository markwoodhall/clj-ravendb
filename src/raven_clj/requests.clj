(ns raven-clj.requests
  (:require [clj-http.client :as client])
  (:require [clojure.data.json :as json]))

(defn load-documents
  "Generates a map that represents a http request
  to the queries endpoint in order to load documents"
  [{:keys [address replications]} document-ids]
  (let [urls (map (fn [u] (str u "/Queries")) replications)
        urls (cons (str address "/Queries") urls)]
    {
     :urls urls
     :body (json/write-str document-ids)
     }))

(defn query-index
  "Generates a map that represents a http request
  to the indexes endpoint in order to query an index."
  [{:keys [address replications]} qry]
  (let [request-url (str "/indexes/" (qry :index) "?query=")
        criteria (clojure.string/join " AND " (into []
                                                    (for [[k v] (dissoc qry :index)]
                                                      (str (name k) ":" v))))
        urls (map (fn [u] (str u request-url criteria)) replications)
        urls (cons (str address request-url criteria) urls)]
    {
     :urls urls
     }))

(defn bulk-operations
  "Generates a map that represents a http request
  to the bulk_docs endpoint in order run document operations."
  [url operations]
  (let [request-url (str url "/bulk_docs")]
    {
     :url request-url
     :body (json/write-str operations)
     }))

(defn put-document
  "Generates a map that represents a http request
  to the bulk_docs endpoint in order put a document."
  [url key document]
  (bulk-operations url [{
                         :Method "PUT"
                         :Key key
                         :Document document
                         :Metadata { }
                         }]))

(defn put-index
  "Generates a map that represents a http request
  to the indexes endpoint in order to put an index."
  [url idx]
  (let [request-url (str url "/indexes/" (idx :name))]
    {
     :url request-url
     :body (json/write-str {:Map (str 
                                   "from " (idx :alias) " in docs"
                                   " where " (idx :where) 
                                   " select " (idx :select))})
     }))

(defn load-replications
  "Generates a map that represents a http request
  to the replication endpoint in order to find replication endpoints."
  [url]
  (let [request-url (str url "/docs/Raven/Replication/Destinations")]
    {
     :url request-url 
     }))