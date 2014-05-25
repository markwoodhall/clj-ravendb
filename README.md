# raven-clj

A clojure library designed to consume a RavenDB rest api. 

## Usage

Loading documents by id.
---
!#clojure
(ns raven-clj.client-loading-documents-test
  (:require [clojure.test :refer :all]
            [raven-clj.client :refer :all]
            [clojure.pprint :as pprint]))

(let [url "http://localhost:8080"
      database "northwind"
      endpoint (endpoint url database)
      doc-ids ["employees/1" "employees/2"]
      actual (load-documents endpoint doc-ids)
      results (actual :results)
      doc-one (first (filter 
                       (fn [i] 
                         (and (= (i :key) "employees/1")
                              (= (-> i :doc :LastName) "Davolio"))) results))
      doc-two (first (filter 
                       (fn [i] 
                         (and (= (i :key) "employees/2")
                              (= (-> i :doc :LastName) "Fuller"))) results))]
---

## License

Copyright © 2014 Mark Woodhall

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
