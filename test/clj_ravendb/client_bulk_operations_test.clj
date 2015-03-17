(ns clj-ravendb.client-bulk-operations-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]))

(let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})]
  (deftest test-bulk-operations-with-no-operations
    (testing "Bulk operations without specifying any operations
             throws an assertion error."
      (is (thrown? AssertionError (bulk-operations! client [])))
      (is (thrown? AssertionError (bulk-operations! client nil)))))

  (deftest test-bulk-operations-with-invalid-operations
    (testing "Bulk operations with invalid operation
             throws an assertion error."
      (is (thrown? AssertionError (bulk-operations! client [{:method "PUT"}])))
      (is (thrown? AssertionError (bulk-operations! client [{:method "DELETE"} {:method "PUT" :id "1" :document {} :metadata {}}])))))

  (deftest test-put-returns-correct-status-code
    (testing "processing a PUT command returns the correct result"
      (let [id "Key1"
            document {:name "Test"}
            actual (bulk-operations! client [{:method "PUT" :document document :id id :metadata {}}])
            expected-status-code 200
            operations (:operations actual)]
        (is (= expected-status-code (actual :status)))
        (is (not-empty operations))
        (is (not= nil (:etag (first operations))))
        (is (= id (:id (first operations))))
        (is (= "PUT" (:method (first operations)))))))

  (use-fixtures :each (fn [f] (f) (bulk-operations! client [{:method "DELETE"
                                                             :id "Key1"}]))))
