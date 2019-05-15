(ns clj-ravendb.client-putting-documents-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clj-ravendb.client :refer [client put-document! bulk-operations!]]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer [ravendb-url ravendb-database oauth-url api-key]]))

(let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})]
  (deftest test-put-returns-correct-status-code
    (testing "processing a PUT command returns the correct result"
      (let [id "Key1"
            document {:name "Test"}
            actual (put-document! client id document)
            expected-status-code 200
            operations (:operations actual)]
        (is (= expected-status-code (actual :status)))
        (is (not-empty operations))
        (is (not= nil (:etag (first operations))))
        (is (not= nil (:id (first operations))))
        (is (not= nil (:method (first operations)))))))

  (use-fixtures :each (fn [f] (f) (bulk-operations! client [{:method "DELETE"
                                                             :id "Key1"}]))))
