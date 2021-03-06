(ns clj-ravendb.rest
  (:require [clj-ravendb.util :refer [post-req put-req get-req del-req debug-do]]
            [clj-ravendb.replication :refer [no-retry-replicas wrap-retry-replicas map-replication-urls]]
            [clj-ravendb.validation :as valid]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clojure.core.memoize :refer [ttl]]
            [clojure.core.async :refer [go chan close! timeout <!! >!! <! >!]]))

(defn- load-documents
  ([client document-ids]
   (load-documents client document-ids {}))
  ([{:keys [enable-oauth? oauth-header] :as client} document-ids
    {:keys [request-builder response-parser]
     :or {request-builder req/load-documents response-parser res/load-documents}}]
   {:pre [(not-empty document-ids)]}
   (-> (request-builder client document-ids)
       (req/wrap-oauth-header oauth-header)
       (wrap-retry-replicas post-req)
       (response-parser))))

(defn- bulk-operations!
  ([client operations]
   (bulk-operations! client operations {}))
  ([{:keys [master-only-writes? enable-oauth? oauth-header] :as client}
    operations
    {:keys [request-builder response-parser]
     :or {request-builder req/bulk-operations response-parser res/bulk-operations}}]
   {:pre [(valid/validate-bulk-operations operations)]}
   (let [request (req/wrap-oauth-header (request-builder client operations) oauth-header)]
     (response-parser (if master-only-writes?
                        (no-retry-replicas request post-req)
                        (wrap-retry-replicas request post-req))))))

(defn- put-index!
  ([client index]
   (put-index! client index {}))
  ([{:keys [enable-oauth? oauth-header] :as client}
    index
    {:keys [request-builder response-parser]
     :or {request-builder req/put-index response-parser res/put-index}}]
   {:pre [(:index index) (:select index)]}
   (-> (request-builder client index)
       (req/wrap-oauth-header oauth-header)
       (put-req)
       (response-parser))))

(defn- delete-index!
  ([client index-name]
   (delete-index! client index-name {}))
  ([{:keys [enable-oauth? oauth-header] :as client}
    index-name
    {:keys [request-builder response-parser]
     :or {request-builder req/delete-index response-parser res/delete-index}}]
   (-> (request-builder client index-name)
       (req/wrap-oauth-header oauth-header)
       (del-req)
       (response-parser))))

(defn- put-document!
  ([client id document]
   (put-document! client id document {}))
  ([client id document options]
   (bulk-operations! client [{:method "PUT"
                              :id id
                              :document (dissoc document :metadata)
                              :metadata (get document :metadata {})}] options)))

(defn- query-index
  ([client query]
   (query-index client query {}))
  ([{:keys [enable-oauth? oauth-header] :as client}
    query
    {:keys [max-attempts wait request-builder response-parser]
     :or {max-attempts 5 wait 100
          request-builder req/query-index
          response-parser res/query-index}}]
   {:pre [(:index query)]}
   (let [get-result #(-> (request-builder client query)
                         (req/wrap-oauth-header oauth-header)
                         (wrap-retry-replicas get-req)
                         (response-parser))]
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

                 (debug-do (println latest))
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

(defn- watch-documents
  ([client document-ids]
   (watch-documents client document-ids (chan)))
  ([client document-ids channel]
   (watch-documents client document-ids channel {}))
  ([client document-ids channel options]
   (watch client #(load-documents client document-ids) channel options)))

(defn- watch-index
  ([client query]
   (watch-index client query (chan)))
  ([client query channel]
   (watch-index client query channel {}))
  ([client query channel options]
   (watch client #(query-index client query) channel options)))

(defn- stats
  ([client]
   (stats client {}))
  ([{:keys [master-only-writes? enable-oauth? oauth-header] :as client}
    {:keys [request-builder response-parser]
     :or {request-builder req/stats response-parser res/stats}}]
   (let [request (req/wrap-oauth-header (request-builder client) oauth-header)]
     (response-parser (wrap-retry-replicas request get-req)))))

(defn- user-info
  ([client]
   (user-info client {}))
  ([{:keys [master-only-writes? enable-oauth? oauth-header] :as client}
    {:keys [request-builder response-parser]
     :or {request-builder req/user-info response-parser res/user-info}}]
   (let [request (req/wrap-oauth-header (request-builder client) oauth-header)]
     (response-parser (wrap-retry-replicas request get-req)))))

(defn rest-client
  "Gets a client for a RavenDB endpoint at the
  given url and database.

  Optionally takes a map of options.
  :replicated? is used to find replicated endpoints.
  :master-only-writes? is used to indicate that write operations only go to the master"
  [url database {:keys [replicated? master-only-writes? enable-oauth? oauth-url oauth-expiry-seconds api-key ssl-insecure?]
                 :or {replicated? false master-only-writes? true enable-oauth? true oauth-expiry-seconds 600 ssl-insecure? false}}]
  (let [fragments (list url "Databases" database)
        address (clojure.string/join "/" fragments)
        oauth-header (ttl
                       #(if enable-oauth?
                          (:body (get-req (req/oauth-token {:address oauth-url :api-key api-key :ssl-insecure? ssl-insecure?}))))
                       :ttl/threshold (* oauth-expiry-seconds 1000))
        load-replications (fn []
                            (debug-do (println "Loading replication destinations from" address))
                            (-> (req/load-replications {:address address :ssl-insecure? ssl-insecure?})
                                (req/wrap-oauth-header oauth-header)
                                (get-req)
                                (res/load-replications)
                                (:results)))
        replications (if replicated?
                       (load-replications)
                       '())]
    {:replicated? replicated?
     :master-only-writes? master-only-writes?
     :enable-oauth? enable-oauth?
     :ssl-insecure? ssl-insecure?
     :oauth-header oauth-header
     :address address
     :replications (map-replication-urls replications database)
     :load-documents load-documents
     :bulk-operations! bulk-operations!
     :put-document! put-document!
     :put-index! put-index!
     :delete-index! delete-index!
     :query-index query-index
     :watch-index watch-index
     :watch-documents watch-documents
     :stats stats
     :user-info user-info}))
