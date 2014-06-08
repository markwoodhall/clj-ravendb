(ns clj-ravendb.client-loading-documents-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clojure.pprint :as pprint]))

(let [url "http://localhost:8080"
      database "northwind"
      client (client url database)]
  (deftest test-load-documents-with-no-document-ids-throws
    (testing "Loading documents without specifying document ids
             throws an assertion error."
      (is (thrown? AssertionError (load-documents client [])))
      (is (thrown? AssertionError (load-documents client nil)))))

  (deftest test-load-documents-returns-correct-status-code
    (testing "loading documents returns the correct status code"
      (let [doc-ids ["employees/1" "employees/2"]
            actual (load-documents client doc-ids)
            expected 200]
        (pprint/pprint actual)
        (is (= expected (actual :status))))))
  
  (deftest test-load-documents-returns-correct-results
    (testing "loading documents returns the correct results"
      (let [doc-ids ["employees/1" "employees/2"]
            actual (load-documents client doc-ids)
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