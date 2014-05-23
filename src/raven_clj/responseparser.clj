(ns raven-clj.responseparser
  (:require [clojure.data.json :as json]))

(defn parse-load-response
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

(defn parse-cmd-response
  [raw-response]
  {
   :status (get-in raw-response [:status])
   })

(defn parse-putidx-response
  [raw-response]
  {
   :status (get-in raw-response [:status])
   })

(defn parse-qryidx-response
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