(ns clj-ravendb.client-querying-indexes-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]))

(let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})
      qry {:index "Orders/ByCompany" :query {:Count 10}}
      range-index-name "RangeIndexTest"
      range-index {:index range-index-name
                   :from :Products
                   :where [[:> :PricePerUnit 20] [:< :UnitsInStock 10] [:== :UnitsOnOrder 0] [:== :Category "categories/2"]]
                   :select [:Name :PricePerUnit]
                   :fields {:PricePerUnit {:Indexing :Analyzed :Analyzer :StandardAnalyzer :Storage :Yes}}}]

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
        (is (= expected (actual :status))))))

  (deftest test-query-index-returns-total-results
    (testing "querying an index returns the total results"
      (let [actual (query-index client qry)]
        (is (number? (actual :total-results))))))

  (deftest test-query-index-returns-correct-results
    (testing "querying an index returns the correct results"
      (let [actual (query-index client qry)
            results (actual :results)
            doc-one (first (filter
                             (fn [i]
                               (and (= (-> i :document :Company) "companies/38")
                                    (= (-> i :document :Count) 10.0))) results))
            doc-two (first (filter
                             (fn [i]
                               (and (= (-> i :document :Company) "companies/68")
                                    (= (-> i :document :Count) 10.0))) results))]
        (and (is (not= nil doc-one))
             (is (not= nil doc-two))))))

  (deftest test-range-query-index-returns-correct-results
    (testing "range querying an index returns the total results"
      (let [actual (query-index client {:index range-index-name :query {:PricePerUnit [:range 0 24]}})
            results (:results actual)
            total-results (:total-results actual)
            doc-one (first (filter
                             (fn [i]
                               (= (-> i :document :Name) "Chef Anton's Gumbo Mix")) results))]
        (and (is (not= nil doc-one))
             (is (= 1 total-results))))))

  (deftest test-query-index-returns-correct-results-in-correct-order
    (testing "querying an index returns the correct results in the correct order"
      (let [actual (query-index client (assoc qry :sort-by :Total))
            results (actual :results)
            doc-one (:document (first results))
            doc-two (:document (second results))]
        (and (is (= (:Company doc-one) "companies/80"))
             (is (= (:Company doc-two) "companies/30"))))))

  (deftest test-query-index-returns-correct-results-in-correct-descending-order
    (testing "querying an index returns the correct results in the correct descending order"
      (let [actual (query-index client (assoc qry :sort-by :-Total))
            results (actual :results)
            doc-one (:document (first results))
            doc-two (:document (second results))]
        (and (is (= (:Company doc-one) "companies/86"))
             (is (= (:Company doc-two) "companies/84"))))))

  (deftest test-query-index-returns-correct-results-using-paging
    (testing "querying an index returns the correct results using paging"
      (let [results1 (:results (query-index client (-> (assoc qry :sort-by :-Total)
                                                       (assoc :page-size 1)
                                                       (assoc :start 0))))
            results2 (:results (query-index client (-> (assoc qry :sort-by :-Total)
                                                       (assoc :page-size 1)
                                                       (assoc :start 1))))]
        (and (is (= (get-in (first results1) [:document :Company]) "companies/86"))
             (is (= (get-in (first results2) [:document :Company]) "companies/84"))))))

  (deftest test-query-index-with-multiple-clauses-returns-correct-results
    (testing "querying an index with multiple clauses returns the correct results"
      (let [actual (query-index client (assoc qry :query {:Count 10 :Total 6089.9}))
            results (actual :results)
            doc-one (first (filter
                             (fn [i]
                               (and (= (-> i :document :Company) "companies/11")
                                    (= (-> i :document :Count) 10.0)
                                    (= (-> i :document :Total) 6089.9))) results))]
        (and (is (not= nil doc-one))
             (is (= 1 (count results)))))))

  (deftest test-query-index-uses-custom-req-builder
    (testing "querying indexes uses custom request builder"
      (let [req-builder (fn [client query]
                          (throw (Exception. "CustomRequestBuilderError")))]
        (is (thrown-with-msg? Exception #"CustomRequestBuilderError"
                              (query-index client qry {:request-builder req-builder
                                                       :response-parser res/query-index}))))))

  (deftest test-query-index-uses-custom-res-parser
    (testing "querying indexes uses custom response parser"
      (let [res-parser (fn [raw-response]
                         (throw (Exception. "CustomResponseParserError")))]
        (is (thrown-with-msg? Exception #"CustomResponseParserError"
                              (query-index client qry {:request-builder req/query-index
                                                       :response-parser res-parser}))))))

  (use-fixtures :each (fn [f]
                        (put-index! client range-index)
                        (f)
                        (delete-index! client range-index-name))))