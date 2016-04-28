# clj-ravendb

A Clojure library designed to consume a RavenDB HTTP api.

## Status

[![Build Status](https://api.travis-ci.org/markwoodhall/clj-ravendb.svg?branch=master)](https://api.travis-ci.org/repositories/markwoodhall/clj-ravendb)
[![Dependency Status](https://www.versioneye.com/user/projects/57225d18ba37ce00350af2aa/badge.svg?style=flat)](https://www.versioneye.com/user/projects/57225d18ba37ce00350af2aa)

This is currently a work in progress and under active development, it is liable to substantial change and currently has the following features:

* Load documents
* Store new documents
* Delete documents
* Store new indexes
* Query indexes
* Create indexes
* Delete indexes
* OAuth Support
* Replication aware
* Watch document(s) for changes
* Watch index queries for changes

## Installation


```clj-ravendb``` is available from [Clojars](https://clojars.org/clj-ravendb)

#

Add the following to ```project.clj``` ```:dependencies```:

[![Clojars Project](http://clojars.org/clj-ravendb/latest-version.svg)](http://clojars.org/clj-ravendb)


## Usage

```clojure

(:require [clj-ravendb.client :refer :all]))

```

Getting a RavenDB client:

```clojure

(def northwind (client "http://localhost:8080" "northwind"))

```

Loading some documents:

```clojure
(load-documents northwind ["employees/1" "employees/2"])

```

Returns a map with a sequence of results like:

```clojure

{:status 200,
 :results
 ({:id "employees/1",
   :last-modified-date "2013-11-12T14:48:11.2943076Z",
   :etag "01000000-0000-0001-0000-00000000006A",
   :document
   {:Territories ["06897" "19713"],
    :HomePhone "(206) 555-9857",
    :ReportsTo "employees/1",
    :LastName "Davolio",
    :Address
    {:Line1 "507 - 20th Ave. E.\r\nApt. 2A",
     :Line2 nil,
     :City "Seattle",
     :Region "WA",
     :PostalCode "98122",
     :Country "USA"},
    :FirstName "Nancy",
    :Birthday "1948-12-08T00:00:00.0000000",
    :Title "Sales Representative",
    :Extension "5467",
    :Notes nil,
    :HiredAt "1992-05-01T00:00:00.0000000"}}
  {:id "employees/2",
   :last-modified-date "2013-11-12T14:48:11.2943076Z",
   :etag "01000000-0000-0001-0000-000000000069",
   :document
   {:Territories
    ["01581" "01730" "01833" "02116" "02139" "02184" "40222"],
    :HomePhone "(206) 555-9482",
    :ReportsTo nil,
    :LastName "Fuller",
    :Address
    {:Line1 "908 W. Capital Way",
     :Line2 nil,
     :City "Tacoma",
     :Region "WA",
     :PostalCode "98401",
     :Country "USA"},
    :FirstName "Andrew",
    :Birthday "1952-02-19T00:00:00.0000000",
    :Title "Vice President, Sales",
    :Extension "3457",
    :Notes nil,
    :HiredAt "1992-08-14T00:00:00.0000000"}})}
```

Putting a document:

```clojure

(put-document! northwind "Employees/10" { :FirstName "David" :LastName "Smith" :age 50 })

```

Returns a map with a key to indicate the HTTP status:

```clojure

{:status 200}

```

Deleting a document:

```clojure
(bulk-operations! northwind [{:method "DELETE"
                              :id "Key1"}])
```

Returns a map with a key to indicate the HTTP status:

```clojure

{:status 200}

```

Querying an index:

```clojure

(query-index northwind { :index "ByCompany" :query {:Count 10}})

;; Sort
(query-index northwind { :index "ByCompany" :query {:Count 10} :sort-by :Total })

;; Sort Descending
(query-index northwind { :index "ByCompany" :query {:Count 10} :sort-by :-Total })

;; Paging
(query-index northwind { :index "ByCompany" :query {:Count 10} :sort-by :Total :page-size 10 :start 1})

;; By default if the index is stale (query-index) will retry 5 times, waiting
;; 100 milliseconds between each try.

;; If the index is stale retry a maximum of 10 times.
(query-index northwind { :index "ByCompany" :query {:Count 10}} { :max-attempts 10 })

;; If the index is stale retry every 500 milliseconds.
(query-index northwind { :index "ByCompany" :query {:Count 10}} { :wait 500 })

```

Returns a map with a sequence of results like:

```clojure
{:status 200,
 :stale? false,
 :total-results 10,
 :results
 ({:id "companies/38", :etag "01000000-0000-0001-0000-000000000069" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/38", :Count 10.0, :Total 6146.3}}
  {:id "companies/49", :etag "01000000-0000-0001-0000-000000000060" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/49", :Count 10.0, :Total 7176.215}}
  {:id "companies/11", :etag "01000000-0000-0001-0000-000000000068" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/11", :Count 10.0, :Total 6089.9}}
  {:id "companies/30", :etag "01000000-0000-0001-0000-000000000067" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/30", :Count 10.0, :Total 11446.36}}
  {:id "companies/84", :etag "01000000-0000-0001-0000-000000000066" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/84", :Count 10.0, :Total 9182.43}}
  {:id "companies/56", :etag "01000000-0000-0001-0000-000000000065" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/56", :Count 10.0, :Total 12496.2}}
  {:id "companies/55", :etag "01000000-0000-0001-0000-000000000046" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/55", :Count 10.0, :Total 15177.4625}}
  {:id "companies/86", :etag "01000000-0000-0001-0000-000000000064" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/86", :Count 10.0, :Total 9588.425}}
  {:id "companies/59", :etag "01000000-0000-0001-0000-000000000063" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/59", :Count 10.0, :Total 23128.86}}
  {:id "companies/68", :etag "01000000-0000-0001-0000-000000000062" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/68", :Count 10.0, :Total 19343.779}}
  {:id "companies/80", :etag "01000000-0000-0001-0000-000000000061" last-modified-date "2013-11-12T14:48:11.2943076Z" :document {:Company "companies/80", :Count 10.0, :Total 10812.15}})}

```

Creating an index:

```clojure
(put-index! northwind {:index "ExpensiveSweetAndSavouryProductsWithLowStockAndRunningOut" ;; the index name
                       :from :Products  ;; if :from is not specified then all doc collections will be covered.
                       :where [[:> :PricePerUser 20] [:< :UnitsInStock 10] [:== :UnitsOnOrder 0] [:== :Category "categories/2"]] ;; the where clauses
                       :select [:Name]} ;; the fields to select
```

Returns a map with a key to indicate the HTTP status:

```clojure

{:status 200}

```

Deleting an index:

```clojure
(delet-index! northwind "ExpensiveSweetAndSavouryProductsWithLowStockAndRunningOut")
```

Returns a map with a key to indicate the HTTP status:

```clojure

{:status 204}

```


Watching for document changes:

```clojure
;; Watching documents makes use of core.async channels.
;; You can either pass in a channel or have watch-documents create one.
;; It creates a future that continuously calls load-document and monitors
;; the results, each time they are different they are put on the channel.
(def watcher (watch-document northwind ["Companies/80", "Companies/79"]))
(let [ch (chan)]
 (def watcher (watch-documents northwind ["Companies/80","Companies/79"] ch)))

;; You can optionally tell the watcher to wait x milliseconds between 'checks'.
(def watcher (watch-documents northwind ["Companies/80","Companies/79"] ch {:wait 1000}))

;; If you dont pass in a channel, you can access the newly created one
(let [ch (:channel watcher)])

;; You can take from the channel like so
(def change (<!! ch))

;; Or non-blocking in a go block
(go (<! ch))

;; You can stop watching using :stop, this will close the channel and stop
;; the future
((:stop watcher))

```

Watching for index changes:

```clojure
;; Watching documents makes use of core.async channels.
;; You can either pass in a channel or have watch-documents create one.
;; It creates a future that continuosly calls load-document and monitors
;; the results, each time they are different they are put on the channel.
(def watcher (watch-index northwind {:index "SomeIndexToWatch"}))

(let [ch (chan)]
 (def watcher (watch-index northwind {:index "SomeIndexToWatch"} ch)))

;; You can optionally tell the watcher to wait x milliseconds between 'checks'.
(def watcher (watch-index northwind {: index "SomeIndexToWatch"} ch {:wait 1000}))

;; If you dont pass in a channel, you can access the newly created one
(let [ch (:channel watcher)])

;; You can take from the channel like so
(def change (<!! ch))

;; Or non-blocking in a go block
(go (<! ch))

;; You can stop watching using :stop, this will close the channel and stop
;; the future
((:stop watcher))

```

## Options

There are a number of "configuration" options that can be used when creating a client:

### OAuth

To create a client that supports OAuth:

```clojure
(def northwind (client "http://localhost:8080" "northwind" {:enable-oauth? true :oauth-url "http://localhost:8081/oauth" :api-key "API-KEY"}
```

### Caching

To create a client that supports caching:

```clojure

(def northwind (client "http://localhost:8080" "northwind" {:caching :aggressive}))

```

When this option is used documents loaded using `load-documents` will be cached. `put-document!` and `bulk-operations!` will update this local cache as well as the server.

###  Replication

To create a client that supports replication:

```clojure

(def northwind (client "http://localhost:8080" "northwind" {:replicated? true}))

```

When this option is used creating the client will also query the master url for replication destinations. The client will be represented by a map that looks like:

```clojure

{:replicated? true
 :master-only-write? true
 :address "http://localhost:8080"
 :replications ("http://localhost:8081" "http://localhost:8082")}

```

When this client is used to `load-documents` or `query-index` if the master is down then one of the replications will be used.

###  Master Only Write

If you've created a client that supports replication by default write operations will only go to the master, you can change this behaviour using the following:

```clojure

(def northwind (client "http://localhost:8080" "northwind" {:replicated? true :master-only-write? false}))

```

The client will be represented by a map that looks like:

```clojure

{:replicated? true
 :master-only-write? false
 :address "http://localhost:8080"
 :replications ("http://localhost:8081" "http://localhost:8082")}

```

When this client is used to `put-document!`, `put-index!` or for `bulk-operations!` if the master is down then one of the replications will be used for write operations.

## Build & Test

```
lein test
```

The tests for this project run agaist a cloud hosted RavenDB 3.0 instance at [RavenHQ](http://www.ravenhq.com) and use an OAuth version of the client.

All functionality has been tested against RavenDB 2.5 as well.

## License

Copyright © 2016 Mark Woodhall

Released under the MIT License: http://www.opensource.org/licenses/mit-license.php
