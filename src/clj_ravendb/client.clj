(ns clj-ravendb.client
  (:require [clj-http.client :as client]
            [clj-ravendb.requests :as req]
            [clojure.core.async :refer [go chan close! timeout <!! >!! <! >!]]
            [clj-ravendb.responses :as res]))

(def ^:dynamic *debug* false)
(defmacro debug-do [& body]
  (when *debug*
    `(do ~@body)))

(def not-nil? (complement nil?))

(defn- post-req
  [{:keys [url body]}]
  (debug-do (println "Sending HTTP POST to" url "with JSON body " body))
  (client/post url {:body body :as :json-string-keys}))

(defn- put-req
  [{:keys [url body]}]
  (debug-do (println "Sending HTTP PUT to" url "with JSON body " body))
  (client/put url {:body body :as :json-string-keys}))

(defn- get-req
  [{:keys [url]}]
  (debug-do (println "Sending HTTP GET to" url))
  (client/get url {:as :json-string-keys}))

(defn- is-valid-client?
  [{:keys [address replications replicated? master-only-writes?]}]
  {:pre [(not-nil? address) (not-nil? replications) 
         (not-nil? replicated?) (not-nil? master-only-writes?)]}
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
                       (debug-do (println "Failed to execute request using " (first urls)))))]
      (if (not (nil? response))
        response
        (recur (rest urls))))))   

(def client?
  (memoize (fn [client] (is-valid-client? client))))

(defn client
  "Gets a client for a RavenDB endpoint at the
  given url and database. 

  Optionally takes a map of options.
  :replicated? is used to find replicated endpoints.
  :master-only-writes? is used to indicate that write operations only go to the master"
  ([url database]
   (client url database {}))
  ([url database {:keys [replicated? master-only-writes?] 
                  :or {replicated? false master-only-writes? true}}]
   (let [fragments (list url "Databases" database)
         address (clojure.string/join "/" fragments)
         load-replications (fn []
                             (debug-do (println "Loading replication destinations from" address))
                             (:results (res/load-replications (get-req (req/load-replications address)))))
         replications (if replicated? 
                        (load-replications)
                        '())]
    {
     :replicated? replicated?
     :master-only-writes? master-only-writes?
     :address address
     :replications (map (fn 
                          [r] 
                          (let [fragments (list r "Databases" database)]
                            (clojure.string/join "/" fragments))) replications) 
     })))

(defn load-documents
  "Loads a collection of documents represented
  by the given document ids. 

  Optionally takes a map of options.
  :request-builder is a custom request builder fn.
  :response-parser is a customer response parser fn."
  ([client document-ids]
   (load-documents client document-ids {}))
  ([client document-ids 
    {:keys [request-builder response-parser]
     :or {request-builder req/load-documents response-parser res/load-documents}}]
   {:pre [(client? client) (not-empty document-ids)]}
   (-> (request-builder client document-ids)
       (wrap-retry-replicas post-req)
       (response-parser))))

(defn bulk-operations
  "Handles a given set of bulk operations that
  correspond to RavenDB batch req.

  Optionally takes a map of options.
  :request-builder is a custom request builder fn.
  :response-parser is a customer response parser fn."
  ([client operations]
   (bulk-operations client operations {}))
  ([{:keys [master-only-writes?] :as client} 
    operations 
    {:keys [request-builder response-parser]
     :or {request-builder req/bulk-operations response-parser res/bulk-operations}}]
   {:pre [(client? client)
          (not-empty (filter 
                       (comp not nil?) 
                       (map (fn[{:keys [Method Document Metadata Key]}] 
                              (cond 
                                (= Method "PUT") (and Document Metadata Key)
                                (= Method "DELETE") Key)) operations)))]}
   (let [request (req/bulk-operations client operations)]
     (response-parser (if master-only-writes? 
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

  Optionally takes a map of options.
  :request-builder is a custom request builder fn.
  :response-parser is a customer response parser fn."
  ([client index]
   (put-index client index {}))
  ([client index 
    {:keys [request-builder response-parser]
     :or {request-builder req/put-index response-parser res/put-index}}]
   {:pre [(client? client)
          (:name index) (:alias index) (:where index) (:select index)]}
   (-> (request-builder client index)
       (put-req)
       (response-parser))))

(defn put-document 
  "Creates or updates a document by its key. Where 'document'
  is a map.

  Optionally takes a map of options.
  :request-builder is a custom request builder fn.
  :response-parser is a customer response parser fn."
  ([client key document]
   (put-document client key document {}))
  ([{:keys [master-only-writes?] :as client} 
    key document 
    {:keys [request-builder response-parser]
     :or {request-builder req/put-document response-parser res/put-document}}]
   {:pre [(client? client)]}
   (let [request (request-builder client key document)]
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

  Optionally takes a map of options. 
  :max-attempts is the maximum number of times to try
  and hit a non stale index.
  :wat is the time interval to wait before trying to
  hit a non stale index.
  :request-builder is a custom request builder fn.
  :response-parser is a customer response parser fn."
  ([client query]
   (query-index client query {}))
  ([client query {:keys [max-attempts wait request-builder response-parser]
                    :or {max-attempts 5 wait 100 
                         request-builder req/query-index 
                         response-parser res/query-index}}]
   {:pre [(client? client) (:index query)]}
   (let [get-result (fn[]
                      (-> (request-builder client query)
                          (wrap-retry-replicas get-req)
                          (response-parser)))]
     (loop [result (get-result) attempt 0]
       (if (or (not (:stale? result)) 
               (= attempt max-attempts))
         result
         (do
           (debug-do (println "Index" (:index query) "is stale, waiting" wait "ms before trying again."))
           (Thread/sleep wait)
           (recur (get-result) (inc attempt))))))))

(defn- watch
  ([client watch-fn channel]
   (watch client watch-fn channel {}))
  ([client watch-fn channel {:keys [wait]
                             :or {wait 500}}]
   (let [keep-watching? (atom true)
         f (future 
             (debug-do (println "Watching" watch "for changes"))
             (loop [last-value {}]
               (let [latest (watch-fn)]
                 (if (and (not= last-value {})
                          (not= last-value latest))
                   (go (>! channel latest)))
                 (if @keep-watching? 
                   (do 
                     (debug-do (println "Waiting" wait "ms until next 'watch'"))
                     (Thread/sleep wait)
                     (recur latest))))))]
     {:channel channel 
      :stop (fn []
              (debug-do (println "Closing channel" channel "and trying to end future" f))
              (reset! keep-watching? false)
              (close! channel))})))

(defn watch-documents
  "Watch a collections of documents for changes 
  and place the changed document(s) on a channel 
  when there are differences.
  
  Options is a map and can contain,
  :wait - milliseconds to wait between watch calls."
  ([client document-ids]
   (watch-documents client document-ids (chan)))
  ([client document-ids channel]
   (watch-documents client document-ids channel {}))
  ([client document-ids channel options]
   (watch client (fn []
                   (load-documents client document-ids)) channel options)))

(defn watch-index
  "Watch the results of an index query for changes 
  and place the changed result(s) on a channel 
  when there are differences.
  
  Options is a map and can contain, 
  :wait - milliseconds to wait between watch calls."
  ([client query]
   (watch-index client query (chan)))
  ([client query channel]
   (watch-index client query channel {}))
  ([client query channel options]
   (watch client (fn []
                   (query-index client query)) channel options)))
