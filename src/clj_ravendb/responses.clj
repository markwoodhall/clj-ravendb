(ns clj-ravendb.responses
  (:require [clojure.data.json :as json]))

(defn load-replications
  [{:keys [body status]}]
  (let [results (body "Destinations")
        mapped (map (fn[i] (i "Url")) results)]
    { 
     :status status 
     :results mapped 
     }))

(defn load-documents
  [{:keys [body status]}]
  (let [results (body "Results")
        mapped (map (fn
                      [col] 
                      (let [metadata (col "@metadata")]
                        {
                         :key (metadata "@id")
                         :doc (into {} 
                                    (for[[k v] (dissoc col "@metadata")]
                                      [(keyword k) v]))
                         })) results)]
    { 
     :status status 
     :results mapped 
     }))

(defn bulk-operations
  [{:keys [status]}]
  {:status status})

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
        mapped (map (fn
                      [col] 
                      (let [metadata (col "@metadata")]
                        (into {} 
                              (for[[k v] (dissoc col "@metadata")]
                                [(keyword k) v]))
                        )) results)]
    {
     :status status
     :stale? stale?
     :results mapped
     }))