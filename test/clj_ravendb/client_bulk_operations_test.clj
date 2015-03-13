(ns clj-ravendb.client-bulk-operations-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]))

(let [client (client ravendb-url ravendb-database)]
  (deftest test-bulk-operations-with-no-operations
    (testing "Bulk operations without specifying any operations
             throws an assertion error."
      (is (thrown? AssertionError (bulk-operations! client [])))
      (is (thrown? AssertionError (bulk-operations! client nil)))))

  (deftest test-bulk-operations-with-invalid-operations
    (testing "Bulk operations with invalid operation
             throws an assertion error."
      (is (thrown? AssertionError (bulk-operations! client [{:method "PUT"}])))
      (is (thrown? AssertionError (bulk-operations! client [{:method "DELETE"} {:method "PUT" :id "1" :document {} :metadata {}}]))))))
