(ns clj-ravendb.client-watching-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clojure.core.async :refer [go chan thread <!! >!! <! >!]]
            [clojure.pprint :as pprint]
            ))

(let [url "http://localhost:8080"
      database "northwind"
      client (client url database)]
  (deftest test-watching-document-puts-to-channel-on-document-change
    (testing "Watching a document puts to a channel on document change"
      (let [key "TestDocToWatch"
            document {:test 2 :name "WatchedDocument"}
            ch (chan)
            watcher (watch-documents client ["TestDocToWatch"] ch)
            _ (Thread/sleep 1000)
            _ (put-document client key document) 
            actual (first (:results (<!! ch)))] 
        ((:stop watcher))   
        (is (= actual {:key key :doc document})))))

  (deftest test-watching-index-puts-to-channel-on-index-change
    (testing "Watching a index puts to a channel on index change"
      (let [key "TestDocToWatch"
            document {:test 2 :name "WatchedDocument"}
            ch (chan)
            watcher (watch-index client {:index "WatchedDocuments"} ch)
            _ (Thread/sleep 1000)
            _ (put-document client key document) 
            actual (first (:results (<!! ch)))]
        ((:stop watcher))   
        (is (= actual document)))))

  (use-fixtures :each (fn [f] 
                        (put-index client {:name "WatchedDocuments" 
                                           :alias "doc" 
                                           :where "doc.name ==\"WatchedDocument\"" 
                                           :select "new { doc.name }"}) 
                        (bulk-operations client [{
                                                  :Method "PUT"
                                                  :Key "TestDocToWatch"
                                                  :Document {:test 1 :name "WatchedDocument"}
                                                  :Metadata {}
                                                  }])
                        (f) 
                        (bulk-operations client [{
                                                  :Method "DELETE"
                                                  :Key "TestDocToWatch"
                                                  }
                                                 ]))))
