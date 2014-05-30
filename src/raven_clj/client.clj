(ns raven-clj.client
  (:require [clj-http.client :as client]
            [clojure.pprint :as pprint]
            [raven-clj.requests :as req]
            [raven-clj.responses :as res]))

(defn- post-req
  [request]
  (client/post (request :url) {:body (request :body) :as :json-string-keys}))

(defn- put-req
  [request]
  (client/put (request :url) {:body (request :body) :as :json-string-keys}))

(defn- get-req
  [request]
  (client/get (request :url) {:as :json-string-keys}))

(defn- is-valid-endpoint?
  [endpoint]
  (:address endpoint))

(def endpoint?
  (memoize (fn [endpoint] (is-valid-endpoint? endpoint))))

(defn endpoint
  "Gets a client for a RavenDB endpoint at the
  given url and database. 

  Optionally queries RavenDB replication
  to find replicated endpoints."
  ([url database]
   (endpoint url database false))
  ([url database replicated?]
   (let [fragments (list url "Databases" database)
         address (clojure.string/join "/" fragments)
         replications (if replicated? 
                        (:results (res/load-replications (get-req (req/load-replications address))))
                        '())]

     {
      :address address
      :replications replications 
      })))

(defn load-documents
  "Loads a collection of documents represented
  by the given document ids. 

  Optionally takes a request builder fn in order 
  to customize request composition.

  Optionally takes a response parser fn in order
  to customize response parsing."
  ([endpoint document-ids]
   (load-documents endpoint document-ids req/load-documents res/load-documents))
  ([endpoint document-ids request-builder response-parser]
   {:pre [(endpoint? endpoint) (not-empty document-ids)]}
   (let [request (request-builder (:address endpoint) document-ids)
         response (post-req request)]
     (response-parser response))))

(defn bulk-operations
  "Handles a given set of bulk operations that
  correspond to RavenDB batch req.

  Optionally takes a request builder fn in order 
  to customize request composition.

  Optionally takes a response parser fn in order
  to customize response parsing."
  ([endpoint operations]
   (bulk-operations endpoint operations req/bulk-operations res/bulk-operations))
  ([endpoint operations request-builder response-parser]
   {:pre [(endpoint? endpoint)
          (not-empty (filter 
                       (comp not nil?) 
                       (map (fn[op] 
                              (cond 
                                (= (:Method op) "PUT") (and (:Document op) (:Metadata op) (:Key op))
                                (= (:Method op) "DELETE") (:Key op))) operations)))]}
   (let [request (req/bulk-operations (:address endpoint) operations)
         response (post-req request)]
     (response-parser response))))

(defn put-index 
  "Creates or updates an index, where an index takes
  the form:
  idx {
    :name index-name 
    :alias document-alias
    :where where-clause
    :select projection
  }

  Optionally takes a request builder fn in order 
  to customize request composition.

  Optionally takes a response parser fn in order
  to customize response parsing."
  ([endpoint index]
   (put-index endpoint index req/put-index res/put-index))
  ([endpoint index request-builder response-parser]
   {:pre [(endpoint? endpoint)
          (:name index) (:alias index) (:where index) (:select index)]}
   (let [request (req/put-index (:address endpoint) index)
         response (put-req request)]
     (response-parser response))))

(defn put-document 
  "Creates or updates a document by its key. Where 'document'
  is a map.

  Optionally takes a request builder fn in order 
  to customize request composition.

  Optionally takes a response parser fn in order
  to customize response parsing."
  ([endpoint key document]
   (put-document endpoint key document req/put-document res/put-document))
  ([endpoint key document request-builder response-parser]
   {:pre [(endpoint? endpoint)]}
   (let [request (request-builder (:address endpoint) key document)
         response (post-req request)]
     (response-parser response))))

(defn query-index 
  "Query an index, where the 'query' takes the form:
  qry {
    :index index-name
    :x 1
    :y 2                        
  }.

  Optionally takes a request builder fn in order 
  to customize request composition.

  Optionally takes a response parser fn in order
  to customize response parsing."
  ([endpoint query]
   (query-index endpoint query req/query-index res/query-index))
  ([endpoint query request-builder response-parser]
   {:pre [(endpoint? endpoint) (:index query)]}
   (let [request (request-builder (:address endpoint) query)
         response (get-req request)]
     (response-parser response))))