(ns clj-ravendb.client-loading-documents-test
  (:require [clojure.test :refer [deftest testing is]]
            [clj-ravendb.client :refer [client load-documents]]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer [ravendb-url ravendb-database oauth-url api-key]]))

(let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})]
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
        (is (= expected (actual :status))))))

  (deftest test-load-documents-uses-custom-req-builder
    (testing "loading documents uses custom request builder"
      (let [doc-ids ["employees/1" "employees/2"]
            req-builder (fn [client document-ids]
                          (throw (Exception. "CustomRequestBuilderError")))]
        (is (thrown-with-msg? Exception #"CustomRequestBuilderError"
                              (load-documents client doc-ids
                                              {:request-builder req-builder
                                               :response-parser res/load-documents}))))))

  (deftest test-load-documents-uses-custom-res-parser
    (testing "loading documents uses custom response parser"
      (let [doc-ids ["employees/1" "employees/2"]
            res-parser (fn [raw-response]
                         (throw (Exception. "CustomResponseParserError")))]
        (is (thrown-with-msg? Exception #"CustomResponseParserError"
                              (load-documents client doc-ids
                                              {:request-builder req/load-documents
                                               :response-parser res-parser}))))))

  (deftest test-load-documents-returns-correct-results
    (testing "loading documents returns the correct results"
      (let [doc-ids ["employees/1" "employees/2"]
            actual (load-documents client doc-ids)
            results (actual :results)
            doc-one (first (filter
                             (fn [i]
                               (and (= (i :id) "employees/1")
                                    (= (-> i :document :LastName) "Davolio"))) results))
            doc-two (first (filter
                             (fn [i]
                               (and (= (i :id) "employees/2")
                                    (= (-> i :document :LastName) "Fuller"))) results))]
        (and (is (not= nil doc-one))
             (is (not= nil (:last-modified-date doc-one)))
             (is (not= nil (:etag doc-one)))
             (is (not= nil (get-in doc-one  [:document :Address :Region])))
             (is (not= nil doc-two))
             (is (not= nil (:last-modified-date doc-two)))
             (is (not= nil (:etag doc-two)))
             (is (not= nil (get-in doc-one  [:document :Address :Region]))))))))
