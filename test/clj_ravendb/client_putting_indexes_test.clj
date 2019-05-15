(ns clj-ravendb.client-putting-indexes-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clj-ravendb.client :refer [client put-index! delete-index! query-index]]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer [ravendb-url ravendb-database oauth-url api-key]]))

(let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})
      idx-name (str "ExpensiveSweetAndSavouryProductsWithLowStockAndRunningOut" (System/currentTimeMillis))
      idx-name-no-where (str "NoWhere" (System/currentTimeMillis))
      analyzed-idx-name (str "ExpensiveSweetAndSavouryProductsWithLowStockAndRunningOut" (System/currentTimeMillis))
      idx {:index idx-name
           :from :Products
           :where [[:> :PricePerUnit 20] [:< :UnitsInStock 10] [:== :UnitsOnOrder 0] [:== :Category "categories/2"]]
           :select [:Name :PricePerUnit]}
      idx-no-where {:index idx-name-no-where
                    :from :Products
                    :select [:Name :PricePerUnit]}
      analyzed-idx {:index analyzed-idx-name
                    :from :Products
                    :where [[:> :PricePerUnit 20] [:< :UnitsInStock 10] [:== :UnitsOnOrder 0] [:== :Category "categories/2"]]
                    :select [:Name :PricePerUnit]
                    :fields {:PricePerUnit {:Indexing :Analyzed :Analyzer :StandardAnalyzer :Storage :Yes}}}]

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

  (deftest test-put-index-with-no-where-returns-correct-status-code
    (testing "putting an index with no where returns the correct status code"
      (let [actual (put-index! client idx-no-where)
            expected 201]
        (is (= expected (actual :status))))))

  (deftest test-query-put-index-returns-correct-results
    (testing "querying an index returns the correct results"
      (let [actual (query-index client {:index idx-name} {:wait 1000})
            results (sort-by :UnitsInStock (map :document (actual :results)))
            doc-one (first (filter
                             (fn [i]
                               (and (= (-> i :Name) "Chef Anton's Gumbo Mix")
                                    (= (-> i :UnitsInStock) 0))) results))]
        (and (is (not= nil doc-one))))))

  (deftest test-put-index-with-analyzed-field-returns-correct-status-code
    (testing "putting an index with analyzed fields returns the correct status code"
      (let [actual (put-index! client analyzed-idx)
            expected 201]
        (is (= expected (actual :status))))))

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
                        (put-index! client idx)
                        (f)
                        (delete-index! client analyzed-idx-name)
                        (delete-index! client idx-name-no-where)
                        (delete-index! client idx-name))))
