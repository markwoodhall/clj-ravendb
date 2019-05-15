(ns clj-ravendb.replication
  (:require [clj-ravendb.util :as u]))

(defn no-retry-replicas
  "Given a request containing a list of urls capable of servicing
  the request, this function will only execute handle against the
  first url"
  [{:keys [urls] :as request} handle]
  (handle (merge {:url (first urls)} request)))

(defn wrap-retry-replicas
  "Given a request containing a list of urls capable of servicing
  the request, this function will execute handle against each of the
  urls"
  [request handle]
  (loop [urls (:urls request)]
    (let [response (try
                     (handle (merge {:url (first urls)} request))
                     (catch java.net.ConnectException ce
                       (u/debug-do (println "Failed to execute request using " (first urls)))))]
      (if (not (nil? response))
        response
        (recur (rest urls))))))

(defn map-replication-urls
  "Given a collection of replication responses returns a sequence of
  replication url for the given database"
  [replications database]
  (map (fn [r]
         (let [fragments (list r "Databases" database)]
           (clojure.string/join "/" fragments))) replications))
