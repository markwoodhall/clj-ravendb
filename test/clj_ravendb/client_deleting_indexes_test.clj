(ns clj-ravendb.client-deleting-indexes-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]))

(let [caching-client (client ravendb-url ravendb-database {:caching :aggressive :ssl-insecure? true :oauth-url oauth-url :api-key api-key})
      client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})
      indexes [(str "test-index" (System/currentTimeMillis)) (str "test-index" (System/currentTimeMillis))]]
  (doseq [i indexes]
    (put-index! client {:index i
                        :where [[:== :name "TestDocument"]]
                        :select [:name]}))
  (deftest test-delete-index-returns-correct-status-code
    (testing "deleting an index returns the correct status code"
      (let [actual (delete-index! client (first indexes))
            expected 204]
        (is (= expected (actual :status))))))

  (deftest test-delete-index-returns-correct-status-code-when-using-caching-client
    (testing "deleting an index returns the correct status code"
      (let [actual (delete-index! caching-client (second indexes))
            expected 204]
        (is (= expected (actual :status)))))))
