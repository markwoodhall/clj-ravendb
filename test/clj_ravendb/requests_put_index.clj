(ns clj-ravendb.requests-put-index
  (:require [clojure.test :refer :all]
            [clj-ravendb.requests :refer [put-index]]))

(deftest put-index-output
  (testing "put-index returns correct index string for map index"
    (let [client {:address "localhost" :replications [] :ssl-insecure? true}
          expected ""
          index {:index "Test"
                 :from :Products
                 :where [[:> :PricePerUnit 20] [:< :UnitsInStock 10] [:== :UnitsOnOrder 0] [:== :Category "categories/2"]]
                 :select [:Name :PricePerUnit]}
          actual (:body (put-index client index))
          expected  "{\"Fields\":[],\"Map\":\" from doc in docs.Products where doc.PricePerUnit>20 && doc.UnitsInStock<10 && doc.UnitsOnOrder==0 && doc.Category==\\\"categories/2\\\" select new { doc.Name,doc.PricePerUnit }\"}"]
      (is (= expected actual))))

  (testing "put-index returns correct index string for projected select"
    (let [client {:address "localhost" :replications [] :ssl-insecure? true}
          expected ""
          index {:index "Test"
                 :from :Products
                 :where [[:> :PricePerUnit 20] [:< :UnitsInStock 10] [:== :UnitsOnOrder 0] [:== :Category "categories/2"]]
                 :select [[:Name "ProductName"] [:PricePerUnit 10]]}
          actual (:body (put-index client index))
          expected  "{\"Fields\":[],\"Map\":\" from doc in docs.Products where doc.PricePerUnit>20 && doc.UnitsInStock<10 && doc.UnitsOnOrder==0 && doc.Category==\\\"categories/2\\\" select new { Name=\\\"ProductName\\\",PricePerUnit=10 }\"}"]
      (is (= expected actual))))

  (testing "put-index returns correct index string for map reduce index"
    (let [client {:address "localhost" :replications [] :ssl-insecure? true}
          expected ""
          index {:index "Test"
                 :from :Products
                 :where [[:> :PricePerUnit 20] [:< :UnitsInStock 10] [:== :UnitsOnOrder 0] [:== :Category "categories/2"]]
                 :select [[:Name "ProductName"] [:PricePerUnit 10] [:Category :Name]]
                 :group  [[:Category :Name]]
                 :group-select [[:CategoryName [:Category :Name]] [:PricePerUnitGrouped [:Sum :PricePerUnit]]]}
          actual (:body (put-index client index))
          expected  "{\"Fields\":[],\"Map\":\" from doc in docs.Products where doc.PricePerUnit>20 && doc.UnitsInStock<10 && doc.UnitsOnOrder==0 && doc.Category==\\\"categories/2\\\" select new { Name=\\\"ProductName\\\",PricePerUnit=10,doc.Category.Name }\",\"Reduce\":\" from result in results  group result by new { result.Category.Name } into g select new { CategoryName=g.Category.Name,PricePerUnitGrouped=g.Sum(x => x.PricePerUnit) }\"}"]
      (is (= expected actual)))))
