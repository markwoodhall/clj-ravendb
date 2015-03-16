(ns clj-ravendb.responses)

(defn mapify
  [col]
  (into {}
        (for[[k v] (dissoc col "@metadata")]
          [(keyword k) (if (= clojure.lang.PersistentArrayMap (type v))
                         (mapify v)
                         v)])) )

(defn load-replications
  [{:keys [body status]}]
  (let [results (body "Destinations")
        mapped (map (fn[i] (i "Url")) results)]
    {:status status
     :results mapped}))

(defn load-documents
  [{:keys [body status]}]
  (let [results (body "Results")
        mapped (map (fn
                      [col]
                      (let [metadata (col "@metadata")]
                        {:id (metadata "@id")
                         :last-modified-date (metadata "Last-Modified")
                         :etag (metadata "@etag")
                         :document (mapify col)})) results)]
    {:status status
     :results mapped}))

(defn bulk-operations
  [{:keys [status body]}]
  {:status status
   :operations (map (fn [i]
                      {:etag (i "Etag")
                       :method (i "Method")
                       :id (i "Key")}) body)})

(defn put-document
  [raw-response]
  (bulk-operations raw-response))

(defn put-index
  [{:keys [status]}]
  {:status status})

(defn query-index
  [{:keys [body status]}]
  (let [results (body "Results")
        stale? (body "IsStale")
        mapped (map mapify results)]
    {:status status
     :stale? stale?
     :results mapped}))

(defn stats
  [{:keys [body status]}]
  {:status status
   :results (mapify body)})

(defn user-info
  [{:keys [body status]}]
  {:status status
   :info (mapify body)})
