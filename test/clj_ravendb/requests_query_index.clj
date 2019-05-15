(ns clj-ravendb.requests-query-index
  (:require [clojure.test :refer [deftest testing is]]
            [clj-ravendb.requests :refer [query-index]]))

(deftest test-query-index-with-one-clause
  (testing "query index with one clause returns correct request"
    (let [client {:address "localhost" :replications [] :ssl-insecure? true}
          qry {:index "IndexName" :query {:count 1}}
          expected {:ssl-insecure? true :urls ["localhost/indexes/IndexName?query=count:1"]}
          actual (query-index client qry)]
      (is (= expected actual)))))

(deftest test-query-index-with-multiple-clauses
  (testing "query index with multiple clauses returns correct request"
    (let [client {:address "localhost" :replications [] :ssl-insecure? true}
          qry {:index "IndexName" :query {:count 1 :value 2}}
          expected {:ssl-insecure? true :urls ["localhost/indexes/IndexName?query=count:1 AND value:2"]}
          actual (query-index client qry)]
      (is (= expected actual)))))

(deftest test-query-index-with-range-query
  (testing "query index with range query returns correct request"
    (let [client {:address "localhost" :replications [] :ssl-insecure? true}
          qry {:index "IndexName" :query {:count [:range 0 50]}}
          expected {:ssl-insecure? true :urls ["localhost/indexes/IndexName?query=count:[0 TO 50]"]}
          actual (query-index client qry)]
      (is (= expected actual)))))

(deftest test-query-index-with-raw-lucene
  (testing "query index with raw lucene returns correct request"
    (let [client {:address "localhost" :replications [] :ssl-insecure? true}
          qry {:index "IndexName" :query "count:10 AND value:30"}
          expected {:ssl-insecure? true :urls ["localhost/indexes/IndexName?query=count:10 AND value:30"]}
          actual (query-index client qry)]
      (is (= expected actual)))))
