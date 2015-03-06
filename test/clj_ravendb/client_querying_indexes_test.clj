(ns clj-ravendb.client-querying-indexes-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clojure.pprint :as pprint]))

(let [url "http://localhost:8080"
      database "northwind"
      client (client url database)
      qry {:index "Orders/ByCompany" :Count 10}]
  (deftest test-query-index-with-invalid-query
    (testing "Querying an index with an invalid query form."
      (is (thrown? AssertionError
                   (query-index client {})))
      (is (thrown? AssertionError
                   (query-index client {:ind "IndexName"})))))

  (deftest test-query-index-returns-correct-status-code
    (testing "querying an index returns the correct status code"
      (let [actual (query-index client qry)
            expected 200]
        (pprint/pprint actual)
        (is (= expected (actual :status))))))

  (deftest test-query-index-returns-correct-results
    (testing "querying an index returns the correct results"
      (let [actual (query-index client qry)
            results (actual :results)
            doc-one (first (filter
                             (fn [i]
                               (and (= (-> i :Company) "companies/38")
                                    (= (-> i :Count) 10.0))) results))
            doc-two (first (filter
                             (fn [i]
                               (and (= (-> i :Company) "companies/68")
                                    (= (-> i :Count) 10.0))) results))]
        (pprint/pprint actual)
        (and (is (not= nil doc-one))
             (is (not= nil doc-two))))))

  (deftest test-query-index-with-multiple-clauses-returns-correct-results
    (testing "querying an index with multiple clauses returns the correct results"
      (let [actual (query-index client (assoc qry :Total 6089.9))
            results (actual :results)
            doc-one (first (filter
                             (fn [i]
                               (and (= (-> i :Company) "companies/11")
                                    (= (-> i :Count) 10.0)
                                    (= (-> i :Total) 6089.9))) results))]
        (pprint/pprint actual)
        (and (is (not= nil doc-one))
             (is (= 1 (count results)))))))

  (deftest test-query-index-uses-custom-req-builder
    (testing "querying indexes uses custom request builder"
      (let [req-builder (fn [client query]
                          (throw (Exception. "CustomRequestBuilderError")))]
        (is (thrown-with-msg? Exception #"CustomRequestBuilderError"
                              (query-index client qry {
                                                       :request-builder req-builder
                                                       :response-parser res/query-index
                                                       }))))))

  (deftest test-query-index-uses-custom-res-parser
    (testing "querying indexes uses custom response parser"
      (let [res-parser (fn [raw-response]
                         (throw (Exception. "CustomResponseParserError")))]
        (is (thrown-with-msg? Exception #"CustomResponseParserError"
                              (query-index client qry {
                                                       :request-builder req/query-index
                                                       :response-parser res-parser
                                                       })))))))