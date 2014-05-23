(ns ravenclo.client-querying-indexes-test
  (:require [clojure.test :refer :all]
            [ravenclo.client :refer :all]
            [clojure.pprint :as pprint]))

(let [url "http://localhost:8080"
      database "northwind"]
  (deftest test-query-index-returns-correct-status-code
    (testing "querying an index returns the correct status code"
      (let [qry {
                 :index "Orders/ByCompany"
                 :Count 10
                 }
            actual (query-index url database qry)
            expected 200]
        (pprint/pprint actual)
        (is (= expected (actual  :status))))))
  
  (deftest test-query-index-returns-correct-results
    (testing "querying an index returns the correct results"
      (let [qry {
                 :index "Orders/ByCompany"
                 :Count 10
                 }
            actual (query-index url database qry)
            results (actual :results)
            doc-one (first (filter 
                             (fn [i] 
                               (and (= (-> i :doc :Company) "companies/59")
                                    (= (-> i :doc :Count) 10.0))) results))
            doc-two (first (filter 
                             (fn [i] 
                               (and (= (-> i :doc :Company) "companies/68")
                                    (= (-> i :doc :Count) 10.0))) results))]
        (pprint/pprint actual)
        (and (is (not= nil doc-one))
             (is (not= nil doc-two))))))
  
  (deftest test-query-index-with-multiple-clauses-returns-correct-results
    (testing "querying an index with multiple clauses returns the correct results"
      (let [qry {
                 :index "Orders/ByCompany"
                 :Count 10
                 :Total 6089.9
                 }
            actual (query-index url database qry)
            results (actual :results)
            doc-one (first (filter 
                             (fn [i] 
                               (and (= (-> i :doc :Company) "companies/11")
                                    (= (-> i :doc :Count) 10.0)
                                    (= (-> i :doc :Total) 6089.9))) results))]
        (pprint/pprint actual)
        (and (is (not= nil doc-one))
             (is (= 1 (count results))))))))