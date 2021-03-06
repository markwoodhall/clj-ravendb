(ns clj-ravendb.client-debug-test
  (:require [clojure.test :refer [deftest testing is]]
            [clj-ravendb.client :refer [client user-info]]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer [oauth-url api-key ravendb-url ravendb-database]]))

(let [expected [:Remark]]
  (let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})]
    (deftest test-user-info-returns-correct-result
      (testing "user-info returns the correct results"
        (let [actual (:info (user-info client))]
          (doseq [k expected]
            (is (not (nil? (k actual)))))))))

  (let [client (client ravendb-url ravendb-database {:caching :aggressive :ssl-insecure? true :oauth-url oauth-url :api-key api-key})]
    (deftest test-user-info-returns-correct-result-when-using-cache-client
      (testing "user-info returns the correct results when using a cache client"
        (let [actual (:info (user-info client))]
          (doseq [k expected]
            (is (not (nil? (k actual))))))))))
