(ns clj-ravendb.client-caching-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]
            [clojure.pprint :as pprint]))

(comment (let [client (client ravendb-url ravendb-database {:caching? true})]
  (deftest test-load-documents-returns-correct-results
    (testing "documents loaded from cache have a :cached? flag"
      (let [doc-ids ["employees/1"]
            _ (load-documents client doc-ids)
            actual (load-documents client doc-ids)
            results (actual :results)
            doc (first results)]
        (is (:cached? doc))))
    (testing "documents loaded get added to the cache"
      (let [doc-ids ["employees/2"]
            _ (load-documents client doc-ids)
            doc (first (filter (fn [d]
                                 (= (:key d) "employees/2")) @client-cache))]
        (is (= "employees/2" (:key doc)))))
    (testing "put documents get added to the cache"
      (let [doc-id "Key1"
            _ (put-document! client doc-id {})
            doc (first (filter (fn [d]
                                 (= (:key d) doc-id)) @client-cache))]
        (is (= doc-id (:key doc)))))
    (testing "deleted documents get removed from the cache"
      (let [doc-id "Key1"
            _ (put-document! client doc-id {})
            _ (bulk-operations! client [{:Method "DELETE"
                                        :Key doc-id}])
            doc (first (filter (fn [d]
                                 (= (:key d) doc-id)) @client-cache))]
        (is (nil? doc)))))

  (use-fixtures :each (fn [f] (f) (bulk-operations! client [{:Method "DELETE"
                                                            :Key "Key1"}])))))
