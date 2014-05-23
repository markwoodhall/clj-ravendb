(ns raven-clj.client-putting-documents-test
  (:require [clojure.test :refer :all]
            [raven-clj.client :refer :all]
            [clojure.pprint :as pprint]))

(let [url "http://localhost:8080"
      database "northwind"]
  (deftest test-put-returns-correct-status-code
    (testing "processing a PUT command returns the correct result"
      (let [cmds [{
                   :Method "PUT"
                   :Key "Key1"
                   :Document { :name "Test"}
                   :Metadata { }
                   }]
            actual (run-cmds url database cmds)
            expected 200]
        (pprint/pprint actual)
        (is (= expected (actual :status))))))
  
  (use-fixtures :each (fn [f] (f) (run-cmds url database [
                                                          {
                                                           :Method "DELETE"
                                                           :Key "Key1"
                                                           }
                                                          ]))))