(ns clj-ravendb.client-putting-documents-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]
            [clojure.pprint :as pprint]))

(let [client (client ravendb-url ravendb-database)]
  (deftest test-put-returns-correct-status-code
    (testing "processing a PUT command returns the correct result"
      (let [key "Key1"
            document {:name "Test"}
            actual (put-document client key document) expected 200]
        (pprint/pprint actual)
        (is (= expected (actual :status))))))

  (deftest test-put-document-uses-custom-req-builder
    (testing "putting documents uses custom request builder"
      (let [key "Key1"
            document {:name "Test"}
            req-builder (fn [client key document]
                          (throw (Exception. "CustomRequestBuilderError")))]
        (is (thrown-with-msg? Exception #"CustomRequestBuilderError" 
                              (put-document client key document 
                                            {:request-builder req-builder 
                                             :response-parser res/put-document}))))))
  
  (deftest test-put-document-uses-custom-res-parser
    (testing "putting documents uses custom response parser"
      (let [key "Key1"
            document {:name "Test"}
            res-parser (fn [raw-response]
                          (throw (Exception. "CustomResponseParserError")))]
        (is (thrown-with-msg? Exception #"CustomResponseParserError" 
                              (put-document client key document 
                                            {:request-builder req/put-document 
                                             :response-parser res-parser}))))))

  (use-fixtures :each (fn [f] (f) (bulk-operations client [{:Method "DELETE"
                                                            :Key "Key1"}]))))