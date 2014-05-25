(ns raven-clj.client
  (:require [clj-http.client :as client]
            [clojure.pprint :as pprint]
            [raven-clj.requests :as requests]
            [raven-clj.responseparser :as res]))

(defn- post-req
  [request]
  (client/post (request :url) {:body (request :body) :as :json-string-keys}))

(defn- put-req
  [request]
  (client/put (request :url) {:body (request :body) :as :json-string-keys}))

(defn- get-req
  [request]
  (client/get (request :url) {:as :json-string-keys}))

(defn endpoint
  "Gets a client for a RavenDB endpoint at the
  given url and database."
  [url database]
  (let [fragments (list url "Databases" database)
        address (clojure.string/join "/" fragments)]
    (assoc {} :address address)))

(defn load-documents
  "Loads a collection of documents represented
  by the given document ids."
  [endpoint document-ids]
  (let [request (requests/load-documents (:address endpoint) document-ids)
        response (post-req request)]
    (res/parse-load-response response)))

(defn bulk-operations
  "Handles a given set of bulk operations that
  correspond to RavenDB batch requests."
  [endpoint operations]
  (let [request (requests/bulk-operations (:address endpoint) operations)
        response (post-req request)]
    (res/parse-cmd-response response)))

(defn put-index 
  "Creates or updates an index, where an index takes
  the form:
  idx {
    :name index-name 
    :alias document-alias
    :where where-clause
    :select projection
  }"
  [endpoint index]
  (let [request (requests/put-index (:address endpoint) index)
        response (put-req request)]
    (res/parse-putidx-response response)))

(defn put-document 
  "Creates or updates a document by its key. Where 'document'
  is a map."
  [endpoint key document]
  (let [request (requests/put-document (:address endpoint) key document)
        response (post-req request)]
    (res/parse-cmd-response response)))

(defn query-index 
  "Query an index, where the 'query' takes the form:
  qry {
    :index index-name
    :x 1
    :y 2                        
  }."
  [endpoint query]
  (let [request (requests/query-index (:address endpoint) query)
        response (get-req request)]
    (res/parse-qryidx-response response)))