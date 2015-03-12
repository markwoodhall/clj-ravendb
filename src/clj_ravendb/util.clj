(ns clj-ravendb.util
  (:require [clj-http.client :as http]))

(def ^:dynamic *debug* false)
(defmacro debug-do [& body]
  (when *debug*
    `(do ~@body)))

(def not-nil? (complement nil?))

(defn post-req
  [{:keys [url body]}]
  (debug-do (println "Sending HTTP POST to" url "with JSON body " body))
  (http/post url {:body body :as :json-string-keys}))

(defn put-req
  [{:keys [url body]}]
  (debug-do (println "Sending HTTP PUT to" url "with JSON body " body))
  (http/put url {:body body :as :json-string-keys}))

(defn get-req
  [{:keys [url]}]
  (debug-do (println "Sending HTTP GET to" url))
  (http/get url {:as :json-string-keys}))
