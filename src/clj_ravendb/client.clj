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

  *`:replicated?` is used to find replicated endpoints
  *`:master-only-writes?` is used to indicate that write operations only go to the master
  *`:caching?` is used to indicate if documents should be cached locally
  *`:enable-oauth?` is used to enable oauth with RavenDB
  *`:oauth-url` is the oauth url to use
  *`:oauth-expiry-seconds` is the number of seconds to use an oauth token before requesting a new one
  *`:api-key` the api key to use for oauth autentication
  *`:ssl-insecure? use insecure underlying http requests"
  ([url database]
   (client url database {}))
  ([url database {:keys [caching] :as options
                  :or {caching :none}}]
   (case caching
     :aggressive (caching-client url database options)
     :none (rest-client url database options))))

(defn load-documents
  "Loads a collection of documents represented
  by the given document ids.

  Invoke using the following:

  `(load-document client [\"docId1\" \"docId2\"] options)`

  Optionally takes a map of options.

  `:request-builder` is a custom request builder fn.
  `:response-parser` is a customer response parser fn."
  [{:keys [load-documents] :as client} & args]
  (apply load-documents client args))

(defn put-document!
  "Creates or updates a document by its id where 'document'
  is a map.

  Invoke using the following:

  `(put-document! client \"MyDocId\" {})`

  Optionally takes a map of options.

  `:request-builder` is a custom request builder fn.
  `:response-parser` is a customer response parser fn."
  [{:keys [put-document!] :as client} & args]
  (apply put-document! client args))

(defn query-index
  "Query an index, where the 'query' takes the form:
  `(let [qry {:index index-name
             :x 1
             :y 2}])`

  Invoke using the following:

  `(query-index client qry options)`

  Optionally takes a map of options.

  `:max-attempts` is the maximum number of times to try
  and hit a non stale index.
  `:wait` is the time interval to wait before trying to
  hit a non stale index.
  `:request-builder` is a custom request builder fn.
  `:response-parser` is a customer response parser fn."
  [{:keys [query-index] :as client} & args]
  (apply query-index client args))

(defn put-index!
  "Creates or updates an index, where an index takes
  the form:

  `(let [idx {:index index-name
             :where [[:== :field \"value\"]]
             :select [:field]}])`

  Invoke using the following:

  `(put-index! client idx options)`

  Optionally takes a map of options.

  `:request-builder` is a custom request builder fn.
  `:response-parser` is a customer response parser fn."
  [{:keys [put-index!] :as client} & args]
  (apply put-index! client args))

(defn delete-index!
  "Deletes an index matching the index-name.

  Invoke using the following:

  `(delete-index! client \"MyIndexName\")`

  Optionally takes a map of options.

  `:request-builder` is a custom request builder fn.
  `:response-parser` is a customer response parser fn."
  [{:keys [delete-index!] :as client} & args]
  (apply delete-index! client args))

(defn bulk-operations!
  "Handles a given set of bulk operations that
  correspond to RavenDB batch req.

  Invoke using the following:

  `(bulk-operations! client [{:method \"DELETE\" :id \"Key1\"} {:method \"PUT\" :document {} :id \"Key2\" :metadata {}}] options)`

  Optionally takes a map of options.

  `:request-builder` is a custom request builder fn.
  `:response-parser` is a customer response parser fn."
  [{:keys [bulk-operations!] :as client} & args]
  (apply bulk-operations! client args))

(defn watch-documents
  "Watch a collections of documents for changes
  and place the changed document(s) on a channel
  when there are differences.

  Invoke using the following:

  `(watch-documents client [\"doc1\" \"doc2\"])`

  Options is a map and can contain,

  `:wait` - milliseconds to wait between watch calls."
  [{:keys [watch-documents] :as client} & args]
  (apply watch-documents client args))

(defn watch-index
  "Watch the results of an index query for changes
  and place the changed result(s) on a channel
  when there are differences.

  Invoke using the following:

  `(let [qry {:index index-name
             :x 1
             :y 2}]
     (watch-index client qry))`

  Options is a map and can contain,

  `:wait` - milliseconds to wait between watch calls."
  [{:keys [watch-index] :as client} & args]
  (apply watch-index client args))

(defn stats
  "Queries the stats RavenDB endpoint
  in order to provide performance statistics at
  the database level.

  Optionally takes a map of options.

  `:request-builder` is a custom request builder fn.
  `:response-parser` is a customer response parser fn."
  [{:keys [stats] :as client} & args]
  (apply stats client args))

(defn user-info
  "Queries the debug/user-info RavenDB endpoint
  in order to provide debug information about
  the current authenticated (or not) user.

  Optionally takes a map of options.

  `:request-builder` is a custom request builder fn.
  `:response-parser` is a customer response parser fn."
  [{:keys [user-info] :as client} & args]
  (apply user-info client args))
