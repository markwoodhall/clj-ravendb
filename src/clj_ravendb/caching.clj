(ns clj-ravendb.caching
  (:require [clojure.set :refer [difference]]
            [clj-ravendb.rest :as rest]
            [clj-ravendb.util :refer :all]))

(def client-cache (atom []))

(defn load-documents
  "Loads a collection of documents represented
  by the given document ids.

  Wraps a clj-ravendb.rest client for the purposes
  of local caching.

  Optionally takes a map of options.
  :request-builder is a custom request builder fn.
  :response-parser is a customer response parser fn."
  [{:keys [load-documents rest-client] :as client} & args]
  (let [rest-load-documents (:load-documents rest-client)
        doc-ids (first args)
        cached-ids (map :id @client-cache)
        not-cached (difference (set doc-ids) (set cached-ids))]
    (when-let [response (apply rest-load-documents client (assoc (vec args) 1 not-cached))]
      (when-let [results (:results response)]
        (swap! client-cache concat (map (fn [r]
                                          (assoc r :cached? true)) results)))
      {:status (:status response)
       :results (filter (fn [{:keys [id]}] (some #{id} doc-ids)) @client-cache)})))

(defn bulk-operations!
  "Handles a given set of bulk operations that
  correspond to RavenDB batch req.

  Wraps a clj-ravendb.rest client for the purposes
  of local caching.

  Optionally takes a map of options.
  :request-builder is a custom request builder fn.
  :response-parser is a customer response parser fn."
  [{:keys [rest-client] :as client} & args]
  (let [rest-bulk-operations! (:bulk-operations! rest-client)
        ops (first args)
        dels (map :id (filter #(= (:method %) "DELETE") ops))
        puts (filter #(= (:method %) "PUT") ops)
        put-ids (map :id puts)
        {:keys [status operations] :as response} (apply rest-bulk-operations! client args)]
    (if (= 200 status)
      (let [put-results (filter #(= (:method %) "PUT") operations)]
        (reset! client-cache (remove (fn [{:keys [id]}] (some #{id} (concat dels put-ids))) @client-cache))
        (swap! client-cache concat (map (fn [{:keys [id document]}]
                                          {:id id
                                           :last-modified-date (new java.util.Date)
                                           :etag (:etag (last (filter #(= (:id %) id) put-results)))
                                           :cached? true
                                           :document document}) puts))))
    response))

(defn put-document!
  "Creates or updates a document by its key. Where 'document'
  is a map.

  Wraps a clj-ravendb.rest client for the purposes
  of local caching.

  Optionally takes a map of options.
  :request-builder is a custom request builder fn.
  :response-parser is a customer response parser fn."
  [{:keys [put-document! rest-client] :as client} & args]
  (let [doc-id (first args)
        doc (second args)
        opts (if (= 3 (count args))
               (nth args 3)
               {})]
    (bulk-operations! client [{:method "PUT" :id doc-id :document doc :metadata {}}] opts)))

(defn caching-client
  "Gets a client for a RavenDB endpoint at the
  given url and database.

  Wraps a clj-ravendb.rest client for the purposes
  of local caching.

  Operations not suitable for local caching. Like
  put-index!, query-index, watch-index and watch-documents
  dispatch directly to the underlying rest client.

  Optionally takes a map of options.
  :replicated? is used to find replicated endpoints.
  :master-only-writes? is used to indicate that write operations only go to the master"
  [url database {:keys [caching] :as options}]
  (let [rest-client (rest/rest-client url database options)
        {:keys [put-index! query-index watch-index watch-documents stats user-info]} rest-client
        caching {:caching caching
                 :rest-client rest-client
                 :load-documents load-documents
                 :bulk-operations! bulk-operations!
                 :put-document! put-document!
                 :put-index! put-index!
                 :query-index query-index
                 :watch-index watch-index
                 :watch-documents watch-documents
                 :stats stats
                 :user-info user-info}]
    (merge rest-client caching)))
