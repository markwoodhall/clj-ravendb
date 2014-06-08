(ns clj-ravendb.client-putting-documents-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clojure.pprint :as pprint]))

(let [url "http://localhost:8080"
      database "northwind"
      client (client url database)]
  (deftest test-put-returns-correct-status-code
    (testing "processing a PUT command returns the correct result"
      (let [key "Key1"
            document {
                      :name "Test"
                      }
            actual (put-document client key document) expected 200]
        (pprint/pprint actual)
        (is (= expected (actual :status))))))

  (use-fixtures :each (fn [f] (f) (bulk-operations client [
                                                             {
                                                              :Method "DELETE"
                                                              :Key "Key1"
                                                              }
                                                             ]))))