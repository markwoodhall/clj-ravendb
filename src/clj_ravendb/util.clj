(ns clj-ravendb.util
  (:require [clj-http.client :as http]))

(def ^:dynamic *debug* false)
(defmacro debug-do [& body]
  (when *debug*
    `(do ~@body)))

(def not-nil? (complement nil?))

(defn post-req
  [{:keys [url body headers ssl-insecure?]}]
  (debug-do (println "Sending HTTP POST to" url "with JSON body " body))
  (http/post url {:body body :headers headers :as :json-string-keys :insecure? ssl-insecure?}))

(defn put-req
  [{:keys [url body headers ssl-insecure?]}]
  (debug-do (println "Sending HTTP PUT to" url "with JSON body " body))
  (http/put url {:body body :headers headers :as :json-string-keys :insecure? ssl-insecure?}))

(defn get-req
  [{:keys [url headers ssl-insecure?]}]
  (debug-do (println "Sending HTTP GET to" url))
  (http/get url {:headers headers :as :json-string-keys :insecure? ssl-insecure?}))

(defn del-req
  [{:keys [url headers ssl-insecure?]}]
  (debug-do (println "Sending HTTP DELETE to" url))
  (http/delete url {:headers headers :as :json-string-keys :insecure? ssl-insecure?}))

(defn as-seq
  [i]
  (if (sequential? i)
    i
    [i]))
