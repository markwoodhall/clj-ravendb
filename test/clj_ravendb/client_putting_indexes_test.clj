(ns clj-ravendb.client-putting-indexes-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]))

(let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})
      idx {:index "ExpensiveSweetAndSavouryProductsWithLowStockAndRunningOut"
           :from :Products
           :where [[:> :PricePerUser 20] [:< :UnitsInStock 10] [:== :UnitsOnOrder 0] [:== :Category "categories/2"]]
           :select [:Name]}]
  (deftest test-put-index-with-invalid-index-throws
    (testing "Putting an index with an invalid form."
      (is (thrown? AssertionError
                   (put-index! client {})))
      (is (thrown? AssertionError
                   (put-index! client {:index "Test"})))
      (is (thrown? AssertionError
                   (put-index! client {:select "Test"})))
      (is (thrown? AssertionError
                   (put-index! client {:where "Test"})))
      (is (thrown? AssertionError
                   (put-index! client {:index "Test" :where "Where"})))))

  (deftest test-put-index-returns-correct-status-code
    (testing "putting an index returns the correct status code"
      (let [actual (put-index! client idx)
            expected 201]
        (is (= expected (actual :status))))))

  (deftest test-query-put-index-returns-correct-results
    (testing "querying an index returns the correct results"
      (let [_ (put-index! client idx)
            actual (query-index client {:index "ExpensiveSweetAndSavouryProductsWithLowStockAndRunningOut"})
            results (sort-by :UnitsInStock (actual :results))
            doc-one (first (filter
                             (fn [i]
                               (and (= (-> i :Name) "Chef Anton's Gumbo Mix")
                                    (= (-> i :UnitsInStock) 0))) results))]
        (and (is (not= nil doc-one))))))

  (deftest test-putting-index-uses-custom-req-builder
    (testing "putting indexes uses custom request builder"
      (let [req-builder (fn [url index]
                          (throw (Exception. "CustomRequestBuilderError")))]
        (is (thrown-with-msg? Exception #"CustomRequestBuilderError"
                              (put-index! client idx
                                         {:request-builder req-builder
                                          :response-parser res/query-index}))))))

  (deftest test-putting-index-uses-custom-res-parser
    (testing "putting indexes uses custom response parser"
      (let [res-parser (fn [raw-response]
                         (throw (Exception. "CustomResponseParserError")))]
        (is (thrown-with-msg? Exception #"CustomResponseParserError"
                              (put-index! client idx
                                         {:request-builder req/put-index
                                          :response-parser res-parser}))))))
  
  (use-fixtures :each (fn [f]
                        (f)
                        (delete-index! client "ExpensiveSweetAndSavouryProductsWithLowStockAndRunningOut"))))