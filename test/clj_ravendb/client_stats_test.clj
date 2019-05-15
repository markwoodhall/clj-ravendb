(ns clj-ravendb.client-stats-test
  (:require [clojure.test :refer [deftest testing is]]
            [clj-ravendb.client :refer [client stats]]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer [ravendb-url ravendb-database oauth-url api-key]]))

(let [expected [:LastDocEtag :LastAttachmentEtag :CountOfIndexes
                :ApproximateTaskCount :CountOfDocuments :StaleIndexes]]
  (let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})]
    (deftest test-stats-returns-correct-result
      (testing "stats returns the correct results"
        (let [actual (:results (stats client))]
          (doseq [k expected]
            (is (not (nil? (k actual)))))))))

  (let [client (client ravendb-url ravendb-database {:caching :aggressive :ssl-insecure? true :oauth-url oauth-url :api-key api-key})]
    (deftest test-stats-returns-correct-result-when-using-cache-client
      (testing "stats returns the correct results when using a cache client"
        (let [actual (:results (stats client))]
          (doseq [k expected]
            (is (not (nil? (k actual))))))))))
