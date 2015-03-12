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
        cached-ids (map :key @client-cache)
        not-cached (difference (set doc-ids) (set cached-ids))]
    (when-let [response (apply rest-load-documents client (assoc (vec args) 1 not-cached))]
      (when-let [results (:results response)]
        (swap! client-cache concat (map (fn [r]
                                          (assoc r :cached? true)) results)))
      {:status (:status response)
       :results (filter (fn [{:keys [key]}] (some #{key} doc-ids)) @client-cache)})))

(defn bulk-operations!
  "Handles a given set of bulk operations that
  correspond to RavenDB batch req.

  Wraps a clj-ravendb.rest client for the purposes
  of local caching.

  Optionally takes a map of options.
  :request-builder is a custom request builder fn.
  :response-parser is a customer response parser fn."
  [{:keys [bulk-operations! rest-client] :as client} & args]
  (let [rest-bulk-operations! (:bulk-operations! rest-client)
        operations (first args)
        dels (map :Key (filter #(= (:Method %) "DELETE") operations))
        puts (filter #(= (:Method %) "PUT") operations)
        {:keys [status] :as response} (apply rest-bulk-operations! client args)]
    (if (= 200 status)
      (do
        (reset! client-cache (remove (fn [{:keys [key]}] (some #{key} dels)) @client-cache))
        (swap! client-cache concat (map (fn [{:keys [Key Document]}] {:key Key :document Document}) puts))))
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
  (let [rest-put-document! (:put-document! rest-client)
        {:keys [status] :as response} (apply rest-put-document! client args)
        doc (second args)
        id (first args)]
    (if (= 200 status)
      (do
        (reset! client-cache (remove (fn [{:keys [key]}] (some #{key} id)) @client-cache))
        (swap! client-cache conj (merge {:key id :cached? true} doc))))
    response))

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
  [url database options]
  (let [rest-client (rest/rest-client url database options)
        {:keys [put-index! query-index watch-index watch-documents]} rest-client
        caching {:caching? true
                 :rest-client rest-client
                 :load-documents load-documents
                 :bulk-operations! bulk-operations!
                 :put-document! put-document!
                 :put-index! put-index!
                 :query-index query-index
                 :watch-index watch-index
                 :watch-documents watch-documents}]
    (merge rest-client caching)))