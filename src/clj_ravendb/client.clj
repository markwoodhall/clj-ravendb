(ns clj-ravendb.client
  (:require [clj-ravendb.rest :refer :all]))

(defn client
  "Gets a client for a RavenDB endpoint at the
  given url and database.

  Optionally takes a map of options.
  :replicated? is used to find replicated endpoints.
  :master-only-writes? is used to indicate that write operations only go to the master"
  ([url database]
   (client url database {}))
  ([url database options]
   (rest-client url database options)))

(defn load-documents
  [{:keys [load-documents] :as client} & args]
  (apply load-documents client args))

(defn put-document!
  [{:keys [put-document!] :as client} & args]
  (apply put-document! client args))

(defn query-index
  [{:keys [query-index] :as client} & args]
  (apply query-index client args))

(defn put-index!
  [{:keys [put-index!] :as client} & args]
  (apply put-index! client args))

(defn bulk-operations!
  [{:keys [bulk-operations!] :as client} & args]
  (apply bulk-operations! client args))

(defn watch-documents
  [{:keys [watch-documents] :as client} & args]
  (apply watch-documents client args))

(defn watch-index
  [{:keys [watch-index] :as client} & args]
  (apply watch-index client args))
