(ns ravenclo.client
  (:require [clj-http.client :as client]
            [clojure.pprint :as pprint]
            [ravenclo.requestbuilder :as req]
            [ravenclo.responseparser :as res]))

(defn- make-post
  [request]
  (client/post (request :url) {:body (request :body) :as :json-string-keys}))

(defn- make-put
  [request]
  (client/put (request :url) {:body (request :body) :as :json-string-keys}))

(defn- make-get
  [request]
  (client/get (request :url) {:as :json-string-keys}))

(defn load-docs
  [url database doc-ids]
  (let [request (req/build-load-request url database doc-ids)
        response (make-post request)]
    (res/parse-load-response response)))

(defn run-cmds
  [url database cmds]
  (let [request (req/build-cmd-request url database cmds)
        response (make-post request)]
    (res/parse-cmd-response response)))

(defn put-index 
  [url database idx]
  (let [request (req/build-putidx-request url database idx)
        response (make-put request)]
    (res/parse-putidx-response response)))

(defn query-index 
  [url database qry]
  (let [request (req/build-qryidx-request url database qry)
        response (make-get request)]
    (res/parse-qryidx-response response)))