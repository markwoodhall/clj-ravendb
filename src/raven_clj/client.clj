(ns raven-clj.client
  (:require [clj-http.client :as client]
            [clojure.pprint :as pprint]
            [raven-clj.requests :as req]
            [raven-clj.responses :as res]))

(def not-nil? (complement nil?))

(defn- post-req
  [{:keys [url body]}]
  (println "Sending HTTP POST to" url "with JSON body " body)
  (client/post url {:body body :as :json-string-keys}))

(defn- put-req
  [{:keys [url body]}]
  (println "Sending HTTP PUT to" url "with JSON body " body)
  (client/put url {:body body :as :json-string-keys}))

(defn- get-req
  [{:keys [url]}]
  (println "Sending HTTP GET to" url)
  (client/get url {:as :json-string-keys}))

(defn- is-valid-endpoint?
  [{:keys [address replications replicated? master-only-write?]}]
  {:pre [(not-nil? address) (not-nil? replications) 
         (not-nil? replicated?) (not-nil? master-only-write?)]}
  true)

(defn- no-retry-replicas
  [{:keys [urls] :as request} handle]
  (handle (merge {:url (first urls)} request)))   

(defn- wrap-retry-replicas
  [request handle]
  (loop [urls (:urls request)]
    (let [response (try
                     (handle (merge {:url (first urls)} request)) 
                     (catch java.net.ConnectException ce 
                       (println (str "Failed to execute request using " (first urls)))))]
      (if (not (nil? response))
        response
        (recur (rest urls))))))   

(def endpoint?
  (memoize (fn [endpoint] (is-valid-endpoint? endpoint))))

(defn endpoint
  "Gets a client for a RavenDB endpoint at the
  given url and database. 

  Optionally takes a map of options.
  :replicated? is used to find replicated endpoints.
  :master-only-write? is used to indicate that write operations only go to the master"
  ([url database]
   (endpoint url database {}))
  ([url database {:keys [replicated? master-only-write?] 
                  :or {replicated? false master-only-write? true}}]
   (let [fragments (list url "Databases" database)
         address (clojure.string/join "/" fragments)
         load-replications (fn []
                             (println "Loading replication destinations from" address)
                             (:results (res/load-replications (get-req (req/load-replications address)))))
         replications (if replicated? 
                        (load-replications)
                        '())]
    {
     :replicated? replicated?
     :master-only-write? master-only-write?
     :address address
     :replications (map (fn 
                          [r] 
                          (let [fragments (list r "Databases" database)]
                            (clojure.string/join "/" fragments))) replications) 
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
   (let [request (request-builder endpoint document-ids)
         response (wrap-retry-replicas request post-req)]
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
   (let [request (req/bulk-operations endpoint operations)
         master-only-write? (:master-only-write? endpoint)]
     (response-parser (if master-only-write? 
                        (no-retry-replicas request post-req)
                        (wrap-retry-replicas request post-req))))))

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
  ([{:keys [master-only-writes?] :as endpoint} key document request-builder response-parser]
   {:pre [(endpoint? endpoint)]}
   (let [request (request-builder endpoint key document)]
     (response-parser (if master-only-writes?
                        (no-retry-replicas request post-req)
                        (wrap-retry-replicas request post-req))))))

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
   (query-index endpoint query {} req/query-index res/query-index))
  ([endpoint query options]
   (query-index endpoint query options req/query-index res/query-index))
  ([endpoint query {:keys [max-attempts wait]
                    :or {max-attempts 5 wait 100}} request-builder response-parser]
   {:pre [(endpoint? endpoint) (:index query)]}
   (let [get-result (fn
                      []
                      (response-parser 
                        (wrap-retry-replicas 
                          (request-builder endpoint query) get-req)))]
     (loop [result (get-result) attempt 0]
       (if (or (not (:stale? result)) 
               (= attempt max-attempts))
         result
         (do
           (println (str "Index " (:index query) " is stale, waiting " wait "ms before trying again."))
           (Thread/sleep wait)
           (recur (get-result) (inc attempt))))))))

(comment
  (def ep (endpoint "http://localhost:8080" "northwind" {:replicated? false})) 
  (load-documents ep ["Orders/400"])
  (bulk-operations ep [
                             {
                              :Method "PUT"
                              :Key "Key1"
                              :Document {:t "Test"}
                              :Metadata {}
                              }
                             {
                              :Method "DELETE"
                              :Key "Key1"
                              }
                             ])
  (query-index ep {:index "Orders/ByCompany" :Count 10}))