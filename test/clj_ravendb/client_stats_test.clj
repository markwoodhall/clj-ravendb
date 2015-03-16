(ns clj-ravendb.client-stats-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]
            [clojure.pprint :as pprint]))

(let [expected [:LastDocEtag :LastAttachmentEtag :CountOfIndexes
                :ApproximateTaskCount :CountOfDocuments :StaleIndexes]]
  (let [client (client ravendb-url ravendb-database)]
    (deftest test-stats-returns-correct-result
      (testing "stats returns the correct results"
        (let [actual (:results (stats client))]
          (doseq [k expected]
            (is (not (nil? (k actual)))))))))

  (let [client (client ravendb-url ravendb-database {:caching? true})]
    (deftest test-stats-returns-correct-result-when-using-cache-client
      (testing "stats returns the correct results when using a cache client"
        (let [actual (:results (stats client))]
          (doseq [k expected]
            (is (not (nil? (k actual))))))))))
