(ns raven-clj.requests
  (:require [clj-http.client :as client])
  (:require [clojure.data.json :as json]))

(defn load-documents
  "Generates a map that represents a http request
  to the queries endpoint in order to load documents"
  [endpoint document-ids]
  (let [request-url (str endpoint "/Queries")]
    {
     :url request-url
     :body (json/write-str document-ids)
     }))

(defn bulk-operations
  "Generates a map that represents a http request
  to the bulk_docs endpoint in order run document operations."
  [endpoint operations]
  (let [request-url (str endpoint "/bulk_docs")]
    {
     :url request-url
     :body (json/write-str operations)
     }))

(defn put-document
  "Generates a map that represents a http request
  to the bulk_docs endpoint in order put a document."
  [endpoint key document]
  (bulk-operations endpoint [{
                              :Method "PUT"
                              :Key key
                              :Document document
                              :Metadata { }
                              }]))

(defn put-index
  "Generates a map that represents a http request
  to the indexes endpoint in order to put an index."
  [endpoint idx]
  (let [request-url (str endpoint "/indexes/" (idx :name))]
    {
     :url request-url
     :body (json/write-str {:Map (str 
                                   "from " (idx :alias) " in docs"
                                   " where " (idx :where) 
                                   " select " (idx :select))})
     }))

(defn query-index
  "Generates a map that represents a http request
  to the indexes endpoint in order to query an index."
  [endpoint qry]
  (let [request-url (str endpoint "/indexes/" (qry :index) "?query=")
        criteria (clojure.string/join " AND " (into []
                                                    (for [[k v] (dissoc qry :index)]
                                                      (str (name k) ":" v))))]
    {
     :url (str request-url criteria)
     }))