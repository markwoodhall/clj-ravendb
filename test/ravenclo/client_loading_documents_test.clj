(ns ravenclo.client-loading-documents-test
  (:require [clojure.test :refer :all]
            [ravenclo.client :refer :all]
            [clojure.pprint :as pprint]))

(let [url "http://localhost:8080"
      database "northwind"]
  (deftest test-load-docs-returns-correct-status-code
    (testing "loading documents returns the correct status code"
      (let [doc-ids ["employees/1" "employees/2"]
            actual (load-docs url database doc-ids)
            expected 200]
        (pprint/pprint actual)
        (is (= expected (actual :status))))))
  
  (deftest test-load-docs-returns-correct-results
    (testing "loading documents returns the correct results"
      (let [doc-ids ["employees/1" "employees/2"]
            actual (load-docs url database doc-ids)
            results (actual :results)
            doc-one (first (filter 
                             (fn [i] 
                               (and (= (i :key) "employees/1")
                                    (= (-> i :doc :LastName) "Davolio"))) results))
            doc-two (first (filter 
                             (fn [i] 
                               (and (= (i :key) "employees/2")
                                    (= (-> i :doc :LastName) "Fuller"))) results))]
        (pprint/pprint actual)
        (and (is (not= nil doc-one))
             (is (not= nil doc-two)))))))