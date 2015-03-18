(ns clj-ravendb.client-watching-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]
            [clojure.core.async :refer [go chan thread <!! >!! <! >!]]))

(let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})]
  (deftest test-watching-document-puts-to-channel-on-document-change
    (testing "Watching a document puts to a channel on document change"
      (let [id "TestDocToWatch"
            document {:test 2 :name "WatchedDocument"}
            ch (chan)
            watcher (watch-documents client ["TestDocToWatch"] ch {:wait 0})
            _ (Thread/sleep 2000)
            _ (put-document! client id document)
            _ (Thread/sleep 2000)
            actual (first (:results (<!! ch)))
            actual (dissoc actual :last-modified-date)
            actual (dissoc actual :etag)]
        ((:stop watcher))
        (is (= actual {:id id :document document})))))

  (deftest test-watching-index-puts-to-channel-on-index-change
    (testing "Watching an index puts to a channel on index change"
      (let [id "TestDocToWatch"
            document {:test 2 :name "WatchedDocument"}
            ch (chan)
            watcher (watch-index client {:index "WatchedDocuments"} ch {:wait 0})
            _ (Thread/sleep 2000)
            _ (put-document! client id document)
            _ (Thread/sleep 2000)
            actual (first (:results (<!! ch)))]
        ((:stop watcher))
        (is (= actual document)))))

  (use-fixtures :each (fn [f]
                        (put-index! client {:name "WatchedDocuments"
                                            :alias "doc"
                                            :where "doc.name ==\"WatchedDocument\""
                                            :select "new { doc.name }"})
                        (bulk-operations! client [{:method "PUT"
                                                   :id "TestDocToWatch"
                                                   :document {:test 1 :name "WatchedDocument"}
                                                   :metadata {}}])
                        (f)
                        (bulk-operations! client [{:method "DELETE"
                                                   :id "TestDocToWatch"}]))))
