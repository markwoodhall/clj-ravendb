(ns clj-ravendb.client-watching-test
  (:require [clojure.test :refer :all]
            [clj-ravendb.client :refer :all]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer :all]
            [clojure.core.async :refer [go chan thread <!! >!! <! >!]]))

(let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})
      id (str "TestDocToWatch" (System/currentTimeMillis))
      index-name (str "WatchedDocuments" (System/currentTimeMillis))]
  (deftest test-watching-document-puts-to-channel-on-document-change
    (testing "Watching a document puts to a channel on document change"
      (let [document {:test 2 :name id}
            ch (chan)
            watcher (watch-documents client [id] ch {:wait 0})
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
      (let [document {:test 2 :name id}
            ch (chan)
            watcher (watch-index client {:index index-name} ch {:wait 0})
            _ (Thread/sleep 2000)
            _ (put-document! client id document)
            _ (Thread/sleep 2000)
            actual (first (:results (<!! ch)))]
        ((:stop watcher))
        (is (= actual document)))))

  (use-fixtures :each (fn [f]
                        (put-index! client {:index index-name
                                            :where [[:== :name id]]
                                            :select [:name]})
                        (bulk-operations! client [{:method "PUT"
                                                   :id id
                                                   :document {:test 1 :name id}
                                                   :metadata {}}])
                        (f)
                        (delete-index! client index-name)
                        (bulk-operations! client [{:method "DELETE"
                                                   :id id}]))))
