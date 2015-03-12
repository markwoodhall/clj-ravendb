(ns clj-ravendb.client-caching-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.caching :refer [client-cache]]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]
            [clojure.pprint :as pprint]))

(let [client (client ravendb-url ravendb-database {:caching? true})
      _ (pprint/pprint client)]
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
                                 (= (:key d) "employees/2")) @client-cache))]
        (is (= "employees/2" (:key doc)))))
    (testing "put documents get added to the cache"
      (let [doc-id "Key1"
            _ (put-document! client doc-id {})
            doc (first (filter (fn [d]
                                 (= (:key d) doc-id)) @client-cache))]
        (is (= doc-id (:key doc)))))
    (testing "put documents get updated in the cache"
      (let [doc-id "Key1"
            _ (put-document! client doc-id {})
            _ (put-document! client doc-id {:updated 1})
            doc (first (filter (fn [d]
                                 (= (:key d) doc-id)) @client-cache))
            _ (println doc)]
        (is (and (= doc-id (:key doc))
                 (= 1 (:updated doc))))))
    (testing "bulk operations result in add and remove from the cache"
      (let [doc-id "Key1"
            doc-id-2 "Key2"
            doc-id-3 "Key3"
            _ (put-document! client doc-id {})
            _ (put-document! client doc-id-2 {})
            _ (bulk-operations! client [{:Method "DELETE"
                                         :Key doc-id}
                                        {:Method "PUT"
                                         :Document {}
                                         :Metadata {}
                                         :Key doc-id-3}])
            doc (first (filter (fn [d]
                                 (= (:key d) doc-id)) @client-cache))
            doc2 (first (filter (fn [d]
                                 (= (:key d) doc-id-2)) @client-cache))
            doc3 (first (filter (fn [d]
                                 (= (:key d) doc-id-3)) @client-cache))]
        (is (nil? doc))
        (is (= doc-id-2 (:key doc2)))
        (is (= doc-id-3 (:key doc3))))))

  (use-fixtures :each (fn [f] (f) (bulk-operations! client [{:Method "DELETE"
                                                             :Key "Key1"}
                                                            {:Method "DELETE"
                                                             :Key "Key2"}]))))
