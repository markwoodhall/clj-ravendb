(ns raven-clj.client-putting-indexes-test
  (:require [clojure.test :refer :all]
            [raven-clj.client :refer :all]
            [clojure.pprint :as pprint]))

(let [url "http://localhost:8080"
      database "northwind"]
  (deftest test-put-index-returns-correct-status-code
    (testing "putting an index returns the correct status code"
      (let [idx {
                 :name "DocumentsByName"
                 :alias "doc"
                 :where "doc.name == \"Test\""
                 :select "new { doc.name }"
                 }
            actual (put-index url database idx)
            expected 201]
        (pprint/pprint actual)
        (is (= expected (actual  :status)))))))