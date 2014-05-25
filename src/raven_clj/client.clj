(ns raven-clj.client
  (:require [clj-http.client :as client]
            [clojure.pprint :as pprint]
            [raven-clj.requests :as requests]
            [raven-clj.responseparser :as res]))

(defprotocol RavenClient
  "A representation of w wrapper for a RavenDB client"
  (load-documents [this document-ids] "Loads the given document ids")
  (bulk-operations [this operations] "Applies the given RavenDB batch requests")
  (put-index [this index] "Creates or updates a given index")
  (put-document [this key document] "Creates or updated a given document by key")
  (query-index [this query] "Queries the given index"))

(defrecord RavenHttpClient [address])

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
  [url database]
  (let [fragments (list url "Databases" database)
        address (clojure.string/join "/" fragments)]
    (RavenHttpClient. address)))

(extend-type RavenHttpClient RavenClient
  (load-documents
    [this document-ids]
    (let [request (requests/load-documents (:address this) document-ids)
          response (post-req request)]
      (res/parse-load-response response)))

  (bulk-operations
    [this operations]
    (let [request (requests/bulk-operations (:address this) operations)
          response (post-req request)]
      (res/parse-cmd-response response)))

  (put-index 
    [this index]
    (let [request (requests/put-index (:address this) index)
          response (put-req request)]
      (res/parse-putidx-response response)))

  (put-document 
    [this key document]
    (let [request (requests/put-document (:address this) key document)
          response (post-req request)]
      (res/parse-cmd-response response)))

  (query-index 
    [this qry]
    (let [request (requests/query-index (:address this) qry)
          response (get-req request)]
      (res/parse-qryidx-response response))))