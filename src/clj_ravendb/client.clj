(ns clj-ravendb.client
  "The client namespace is the main entry point for the library
  and allows creation of a RavenDB client using the `client` function.

  The API documentation is available [here](http://markwoodhall.github.io/clj-ravendb).
  "
  {:author "Mark Woodhall"}
  (:require [clj-ravendb.rest :refer [rest-client]]
            [clj-ravendb.caching :refer [caching-client]]))

(defn client
  "Gets a client for a RavenDB endpoint at the
  given url and database.

  Optionally takes a map of options.
  :replicated? is used to find replicated endpoints.
  :master-only-writes? is used to indicate that write operations only go to the master
  :caching? is used to indicate if documents should be cached locally"
  ([url database]
   (client url database {}))
  ([url database {:keys [caching] :as options
                  :or {caching :none}}]
   (case caching
     :aggressive (caching-client url database options)
     :none (rest-client url database options))))

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

(defn delete-index!
  [{:keys [delete-index!] :as client} & args]
  (apply delete-index! client args))

(defn bulk-operations!
  [{:keys [bulk-operations!] :as client} & args]
  (apply bulk-operations! client args))

(defn watch-documents
  [{:keys [watch-documents] :as client} & args]
  (apply watch-documents client args))

(defn watch-index
  [{:keys [watch-index] :as client} & args]
  (apply watch-index client args))

(defn stats
  [{:keys [stats] :as client} & args]
  (apply stats client args))

(defn user-info
  [{:keys [user-info] :as client} & args]
  (apply user-info client args))
