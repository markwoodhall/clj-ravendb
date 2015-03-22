(ns clj-ravendb.requests
  (:require [clojure.data.json :as json]
            [clojure.string :refer [join]]
            [cheshire.core :refer [generate-string]]))

(defn- all-urls
  [master replicas url]
  (let [urls (map (fn [u] (str u url)) replicas)
        urls (cons (str master url) urls)]
  urls))

(defn- get-token-headers
  [enable-oauth? api-key]
  (if enable-oauth?
    {"Api-Key" api-key
     "grant_type" "client_credentials"}
    {}))

(defn- oauth-headers
  [enable-oauth? token]
  (if enable-oauth?
    {"Authorization" (str "Bearer " (generate-string token))}
    {}))

(defn load-documents
  "Generates a map that represents a http request
  to the queries endpoint in order to load documents"
  [{:keys [address replications enable-oauth? oauth-header ssl-insecure?]} document-ids]
  {:ssl-insecure? ssl-insecure?
   :urls (all-urls address replications "/Queries")
   :body (json/write-str document-ids)})

(defn query-index
  "Generates a map that represents a http request
  to the indexes endpoint in order to query an index."
  [{:keys [address replications ssl-insecure?]} qry]
  (let [request-url (str "/indexes/" (qry :index) "?query=")
        criteria (clojure.string/join " AND " (vec (for [[k v] (dissoc qry :index)]
                                                     (str (name k) ":" v))))]
    {:ssl-insecure? ssl-insecure?
     :urls (all-urls address replications (str request-url criteria))}))

(defn bulk-operations
  "Generates a map that represents a http request
  to the bulk_docs endpoint in order run document operations."
  [{:keys [address replications ssl-insecure?]} operations]
  (let [request-url (str address "/bulk_docs")]
    {:urls (all-urls address replications "/bulk_docs")
     :ssl-insecure? ssl-insecure?
     :body (json/write-str (map (fn [{:keys [method id document metadata]}]
                                  {:Method method
                                   :Key id
                                   :Document document
                                   :Metadata metadata}) operations))}))

(defn put-document
  "Generates a map that represents a http request
  to the bulk_docs endpoint in order put a document."
  [client id document]
  (bulk-operations client [{:method "PUT"
                            :id id
                            :document document
                            :metadata { }}]))

(defn put-index
  "Generates a map that represents a http request
  to the indexes endpoint in order to put an index."
  [{:keys [address ssl-insecure?]} {:keys [index from where select]
                                    :or {from "docs"}}]
  (let [request-url (str address "/indexes/" index)
        from (if (= "docs" from)
               from
               (str "docs." (name from)))
        where (join " && " (map (fn [w]
                                  (let [value (nth w 2)
                                        esc-value (if (= java.lang.String (class value))
                                                    (str \" value \")
                                                    value)]
                                    (str "doc." (name (second w)) (name (first w)) esc-value))) where))
        select (str "new { " (join "," (map #(str "doc." (name %)) select)) " }")]
    {:url request-url
     :ssl-insecure? ssl-insecure?
     :body (str "{Map:'"
              "from doc in " from \return\newline
              "where " where \return\newline
              "select " select "'}")}))

(defn delete-index
  "Generates a map that represents a http request
  to the indexes/indexName endpoint in order to delete the index."
  [{:keys [address ssl-insecure?]} index-name]
  (let [request-url (str address "/indexes/" index-name)]
    {:url request-url
     :ssl-insecure? ssl-insecure?}))

(defn load-replications
  "Generates a map that represents a http request
  to the replication endpoint in order to find replication endpoints."
  [{:keys [address ssl-insecure?]}]
  (let [request-url (str address "/docs/Raven/Replication/Destinations")]
    {:url request-url
     :ssl-insecure? ssl-insecure?}))

(defn stats
  "Generates a map that represents a http request
  to the /stats RavenDB endpoint"
  [{:keys [address replications ssl-insecure?]}]
  {:ssl-insecure? ssl-insecure?
   :urls (all-urls address replications "/stats")})

(defn user-info
  "Generates a map that represents a http request
  to the /debug/user-info RavenDB endpoint"
  [{:keys [address replications ssl-insecure?]}]
  {:ssl-insecure? ssl-insecure?
   :urls (all-urls address replications "/debug/user-info")})

(defn oauth-token
  "Generates a map that represents a http request
  to the /ApiKeys/OAuth/AccessToken RavenDB endpoint"
  [{:keys [address replications enable-oauth? api-key ssl-insecure?]}]
  {:headers (get-token-headers enable-oauth? api-key)
   :ssl-insecure? ssl-insecure?
   :url (str address "/OAuth/AccessToken")})

(defn wrap-oauth-header
  [request enable-oauth? oauth-header]
  (merge request {:headers (oauth-headers enable-oauth? oauth-header)}))
