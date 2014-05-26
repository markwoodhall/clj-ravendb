(ns raven-clj.responses
  (:require [clojure.data.json :as json]))

(defn load-documents
  [raw-response]
  (let [body (raw-response :body)
        status (raw-response :status)
        results (body "Results")
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
  [raw-response]
  {
   :status (get-in raw-response [:status])
   })

(defn put-document
  [raw-response]
  (bulk-operations raw-response))

(defn put-index
  [raw-response]
  {
   :status (get-in raw-response [:status])
   })

(defn query-index
  [raw-response]
  (let [body (raw-response :body)
        status (raw-response :status)
        results (body "Results")
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