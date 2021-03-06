(ns clj-ravendb.client-watching-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [clj-ravendb.client :refer [client watch-documents put-document! watch-index delete-index! put-index! bulk-operations!]]
            [clj-ravendb.requests :as req]
            [clj-ravendb.responses :as res]
            [clj-ravendb.config :refer [ravendb-url ravendb-database oauth-url api-key]]
            [clojure.core.async :refer [go chan thread <!! >!! <! >!]]))

(let [client (client ravendb-url ravendb-database {:ssl-insecure? true :oauth-url oauth-url :api-key api-key})
      id (str "TestDocToWatch" (System/currentTimeMillis))
      index-name (str "WatchedDocuments" (System/currentTimeMillis))]
  (deftest test-watching-document-puts-to-channel-on-document-change
    (testing "Watching a document puts to a channel on document change"
      (let [document {:test 2 :name id}
            ch (chan)
            watcher (watch-documents client [id] ch {:wait 0})
            _ (Thread/sleep 4000)
            _ (put-document! client id document)
            _ (Thread/sleep 4000)
            actual (:document (first (:results (<!! ch))))
            actual (dissoc actual :metadata)]
        ((:stop watcher))
        (is (= actual document)))))

  (deftest test-watching-index-puts-to-channel-on-index-change
    (testing "Watching an index puts to a channel on index change"
      (let [document {:test 2 :name id}
            ch (chan)
            _ (Thread/sleep 4000)
            watcher (watch-index client {:index index-name} ch {:wait 0})
            _ (Thread/sleep 4000)
            _ (put-document! client id document)
            _ (Thread/sleep 4000)
            actual (dissoc (first (filter (fn [r] (= (:name r) id)) (map :document (:results (<!! ch))))) :metadata)]
        ((:stop watcher))
        (is (= actual document)))))

  (use-fixtures :each (fn [f]
                        (put-index! client {:index index-name
                                            :where [[:== :name id]]
                                            :select [:name]})
                        (f)
                        (delete-index! client index-name)
                        (bulk-operations! client [{:method "DELETE"
                                                   :id id}]))))
