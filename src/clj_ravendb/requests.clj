(ns clj-ravendb.requests
  (:require [clojure.string :refer [join]]
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
   :body (generate-string document-ids)})

(defn query-index
  "Generates a map that represents a http request
  to the indexes endpoint in order to query an index."
  [{:keys [address replications ssl-insecure?]} {:keys [index sort-by page-size start query]}]
  (let [request-url (str "/indexes/" index "?query=")
        criteria (clojure.string/join " AND " (vec (for [[k v] query]
                                                     (str (name k) ":" v))))
        page-criteria (if page-size
                        (str "&pageSize=" page-size)
                        "")
        start-criteria (if start
                         (str "&start=" start)
                         "")
        sort-criteria (if sort-by
                        (str "&sort=" (name sort-by))
                        "")]
    {:ssl-insecure? ssl-insecure?
     :urls (all-urls address replications (str request-url criteria sort-criteria start-criteria page-criteria))}))

(defn bulk-operations
  "Generates a map that represents a http request
  to the bulk_docs endpoint in order run document operations."
  [{:keys [address replications ssl-insecure?]} operations]
  (let [request-url (str address "/bulk_docs")]
    {:urls (all-urls address replications "/bulk_docs")
     :ssl-insecure? ssl-insecure?
     :body (generate-string (map (fn [{:keys [method id document metadata]}]
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
  [{:keys [address ssl-insecure?]} {:keys [index from where select fields]
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
        select (str "new { " (join "," (map #(str "doc." (name %)) select)) " }")
        field-names (map name (keys fields))
        field-names (if field-names
                 field-names
                 [])
        indexes (reduce merge (map #(if (not= :No (:Indexing (apply (key %) %)))
                                     {(keyword (key %)) (name (:Indexing (apply (key %) %)))}) fields))
        stores (reduce merge (map #(if (= :Yes (:Storage (apply (key %) %)))
                                     {(keyword (key %)) "Yes"}) fields))
        analyzers (reduce merge (map #(if (:Analyzer (apply (key %) %))
                                     {(keyword (key %)) (name (:Analyzer (apply (key %) %)))}) fields))
        index  {:Fields field-names
                :Map (str " from doc in " from
                          " where " where
                          " select " select)}
        index (if (empty? indexes)
                index
                (assoc index :Indexes indexes))
        index (if (empty? stores)
                index
                (assoc index :Stores stores))
        index (if (empty? analyzers)
                index
                (assoc index :Analyzers analyzers))]
    {:url request-url
     :ssl-insecure? ssl-insecure?
     :body (generate-string index)}))

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
