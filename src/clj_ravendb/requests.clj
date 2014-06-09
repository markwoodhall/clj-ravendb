(ns clj-ravendb.requests
  (:require [clj-http.client :as client])
  (:require [clojure.data.json :as json]))

(defn- all-urls
  [master replicas url]
  (let [urls (map (fn [u] (str u url)) replicas)
        urls (cons (str master url) urls)]
  urls))

(defn load-documents
  "Generates a map that represents a http request
  to the queries endpoint in order to load documents"
  [{:keys [address replications]} document-ids]
  {
   :urls (all-urls address replications "/Queries")
   :body (json/write-str document-ids)
   })

(defn query-index
  "Generates a map that represents a http request
  to the indexes endpoint in order to query an index."
  [{:keys [address replications]} qry]
  (let [request-url (str "/indexes/" (qry :index) "?query=")
        criteria (clojure.string/join " AND " (into []
                                                    (for [[k v] (dissoc qry :index)]
                                                      (str (name k) ":" v))))]
    {
     :urls (all-urls address replications (str request-url criteria))
     }))

(defn bulk-operations
  "Generates a map that represents a http request
  to the bulk_docs endpoint in order run document operations."
  [{:keys [address replications]} operations]
  (let [request-url (str address "/bulk_docs")]
    {
     :urls (all-urls address replications "/bulk_docs")
     :body (json/write-str operations)
     }))

(defn put-document
  "Generates a map that represents a http request
  to the bulk_docs endpoint in order put a document."
  [client key document]
  (bulk-operations client [{
                            :Method "PUT"
                            :Key key
                            :Document document
                            :Metadata { }
                            }]))

(defn put-index
  "Generates a map that represents a http request
  to the indexes endpoint in order to put an index."
  [{:keys [address]} {:keys [name alias where select]}]
  (let [request-url (str address "/indexes/" name)]
    {
     :url request-url
     :body (json/write-str {:Map (str 
                                   "from " alias " in docs"
                                   " where " where
                                   " select " select)})
     }))

(defn load-replications
  "Generates a map that represents a http request
  to the replication endpoint in order to find replication endpoints."
  [url]
  (let [request-url (str url "/docs/Raven/Replication/Destinations")]
    {
     :url request-url 
     }))