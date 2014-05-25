(ns raven-clj.client-putting-indexes-test
  (:require [clojure.test :refer :all]
            [raven-clj.client :refer :all]
            [clojure.pprint :as pprint]))

(let [url "http://localhost:8080"
      database "northwind"
      endpoint (endpoint url database)]
  (deftest test-put-index-with-invalid-index-throws
    (testing "Putting an index with an invalid form."
      (is (thrown? AssertionError 
                   (put-index endpoint {})))
      (is (thrown? AssertionError 
                   (put-index endpoint {
                                        :name "Test"
                                        })))
      (is (thrown? AssertionError 
                   (put-index endpoint {
                                        :name "Test" 
                                        :alias "Alias"
                                        })))
      (is (thrown? AssertionError 
                   (put-index endpoint {
                                        :name "Test"
                                        :alias "Alias"
                                        :where "Where"
                                        })))))

  (deftest test-put-index-returns-correct-status-code
    (testing "putting an index returns the correct status code"
      (let [idx {
                 :name "DocumentsByName"
                 :alias "doc"
                 :where "doc.name == \"Test\""
                 :select "new { doc.name }"
                 }
            actual (put-index endpoint idx)
            expected 201]
        (pprint/pprint actual)
        (is (= expected (actual  :status)))))))