(ns clj-ravendb.client-caching-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.caching :refer [client-cache]]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]))

(let [client (client ravendb-url ravendb-database {:caching :aggressive :ssl-insecure? true :oauth-url oauth-url :api-key api-key})]
  (deftest test-load-documents-returns-correct-results
    (testing "documents loaded from cache have a :cached? flag"
      (let [doc-ids ["employees/1"]
            _ (load-documents client doc-ids)
            actual (load-documents client (conj doc-ids "employees/2"))
            results (actual :results)
            doc (first results)
            doc-2 (second results)]
        (is (:cached? doc))
        (is (nil? (:cached doc-2)))))
    (testing "documents loaded get added to the cache"
      (let [doc-ids ["employees/2"]
            _ (load-documents client doc-ids)
            doc (first (filter (fn [d]
                                 (= (:id d) "employees/2")) @client-cache))]
        (is (= "employees/2" (:id doc)))))
    (testing "put documents get added to the cache"
      (let [doc-id "Key1"
            _ (put-document! client doc-id {})
            doc (first (filter (fn [d]
                                 (= (:id d) doc-id)) @client-cache))]
        (is (= doc-id (:id doc)))
        (is (not= nil (:etag doc)))
        (is (= true (:cached? doc)))
        (is (not= nil (:last-modified-date doc)))))
    (testing "put documents get updated in the cache"
      (let [doc-id "Key1"
            _ (put-document! client doc-id {})
            _ (put-document! client doc-id {:updated 1})
            doc (first (filter (fn [d]
                                 (= (:id d) doc-id)) @client-cache))]
        (is (and (= doc-id (:id doc))
                 (= 1 (get-in doc [:document :updated]))))))
    (testing "bulk operations result in add and remove from the cache"
      (let [doc-id "Key1"
            doc-id-2 "Key2"
            doc-id-3 "Key3"
            _ (put-document! client doc-id {})
            _ (put-document! client doc-id-2 {})
            _ (bulk-operations! client [{:method "DELETE"
                                         :id doc-id}
                                        {:method "PUT"
                                         :document {}
                                         :metadata {}
                                         :id doc-id-3}])
            doc (first (filter (fn [d]
                                 (= (:id d) doc-id)) @client-cache))
            doc2 (first (filter (fn [d]
                                 (= (:id d) doc-id-2)) @client-cache))
            doc3 (first (filter (fn [d]
                                 (= (:id d) doc-id-3)) @client-cache))]
        (is (nil? doc))
        (is (= doc-id-2 (:id doc2)))
        (is (= doc-id-3 (:id doc3))))))

  (use-fixtures :each (fn [f] (f) (bulk-operations! client [{:method "DELETE"
                                                             :id "Key1"}
                                                            {:method "DELETE"
                                                             :id "Key2"}
                                                            {:method "DELETE"
                                                             :id "Key3"}]))))
