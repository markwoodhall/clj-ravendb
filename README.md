# raven-clj

A clojure library designed to consume a RavenDB rest api. 

## Status

This is currently a work in progress and under active development, its not stable or production ready.  

## Usage

```
#!clojure

(:require [raven-clj.client :refer :all]))

```

Getting an endpoint:

```
#!clojure

(def endpoint (endpoint "http://localhost:8080" "northwind"))

```

Loading some documents:

```
#!clojure
(load-documents endpoint ["employees/1" "employees/2"])

```

Returns a map with a sequence of results like:

```
#!clojure

{:status 200,
 :results
 ({:key "employees/1",
   :doc
   {:Territories ["06897" "19713"],
    :HomePhone "(206) 555-9857",
    :ReportsTo "employees/1",
    :LastName "Davolio",
    :Address
    {"Line1" "507 - 20th Ave. E.\r\nApt. 2A",
     "Line2" nil,
     "City" "Seattle",
     "Region" "WA",
     "PostalCode" "98122",
     "Country" "USA"},
    :FirstName "Nancy",
    :Birthday "1948-12-08T00:00:00.0000000",
    :Title "Sales Representative",
    :Extension "5467",
    :Notes nil,
    :HiredAt "1992-05-01T00:00:00.0000000"}}
  {:key "employees/2",
   :doc
   {:Territories
    ["01581" "01730" "01833" "02116" "02139" "02184" "40222"],
    :HomePhone "(206) 555-9482",
    :ReportsTo nil,
    :LastName "Fuller",
    :Address
    {"Line1" "908 W. Capital Way",
     "Line2" nil,
     "City" "Tacoma",
     "Region" "WA",
     "PostalCode" "98401",
     "Country" "USA"},
    :FirstName "Andrew",
    :Birthday "1952-02-19T00:00:00.0000000",
    :Title "Vice President, Sales",
    :Extension "3457",
    :Notes nil,
    :HiredAt "1992-08-14T00:00:00.0000000"}})}
```

Putting a document:

```
#!clojure

(put-document endpoint "Employees/10" { :FirstName "David" :LastName "Smith" :age 50 })

```

Returns a map with a key to indicate the HTTP status:

```
#!clojure 

{:status 200}

```

Querying an index:

```
#!clojure

(query-index endpoint { :index "ByCompany" :Count 10 })

;; By default if the index is stale (query-index) will retry 5 times, waiting
;; 100 milliseconds between each try.

;; If the index is stale retry a maximum of 10 times.
(query-index endpoint { :index "ByCompany" :Count 10 } { :max-attempts 10 })

;; If the index is stale retry every 500 milliseconds.
(query-index endpoint { :index "ByCompany" :Count 10 } { :wait 500 })

```

Returns a map with a sequence of results like:

```
#!clojure

{:status 200
 :stale? false ;; indicates if the queried index is currently stale
 :results
 ({:key nil,
   :doc {:Company "companies/38", :Count 10.0, :Total 6146.3}}
  {:key nil,
   :doc {:Company "companies/49", :Count 10.0, :Total 7176.215}}
  {:key nil,
   :doc {:Company "companies/11", :Count 10.0, :Total 6089.9}}
  {:key nil,
   :doc {:Company "companies/30", :Count 10.0, :Total 11446.36}}
  {:key nil,
   :doc {:Company "companies/84", :Count 10.0, :Total 9182.43}}
  {:key nil,
   :doc {:Company "companies/56", :Count 10.0, :Total 12496.2}}
  {:key nil,
   :doc {:Company "companies/55", :Count 10.0, :Total 15177.4625}}
  {:key nil,
   :doc {:Company "companies/86", :Count 10.0, :Total 9588.425}}
  {:key nil,
   :doc {:Company "companies/59", :Count 10.0, :Total 23128.86}}
  {:key nil,
   :doc {:Company "companies/68", :Count 10.0, :Total 19343.779}}
  {:key nil,
   :doc {:Company "companies/80", :Count 10.0, :Total 10812.15}})}
   
```

## Options

There are a number of "configuration" options that can be used when creating an endpoint. 

###  Replication

To create an endpoint that supports replication:

```
#!clojure

(def endpoint (endpoint "http://localhost:8080" "northwind" {:replicated? true}))

```

When this option is used creating the endpoint will also query the master url for replication destinations. The endpoint will be represented by a map that looks like:

```
#!clojure

{
  :replicated? true
  :master-only-write? true
  :address "http://localhost:8080"
  :replications ("http://localhost:8081" "http://localhost:8082")
}

```

When this endpoint is used to (load-documents) or (query-index) if the master is down then one of the replications will be used.

###  Master Only Write

If you've created an endpoint that supports replication by default write operations will only go to the master, you can change this behaviour using the following:

```
#!clojure

(def endpoint (endpoint "http://localhost:8080" "northwind" {:replicated? true :master-only-write? false}))

```

The endpoint will be represented by a map that looks like:

```
#!clojure

{
  :replicated? true
  :master-only-write? false
  :address "http://localhost:8080"
  :replications ("http://localhost:8081" "http://localhost:8082")
}

```

When this endpoint is used to (put-document), (put-index) or for (bulk-operations) if the master is down then one of the replications will be used for write operations.

## Build & Test

lein test

The tests for this project assume an instance of RavenDB is running at http://localhost:8080. They also assume that the instance contains the sample northwind database. The sample northwind database is available [here](https://github.com/ayende/ravendb/blob/2.5/Raven.Studio/Assets/EmbeddedData/Northwind.dump)

## License

Copyright © 2014 Mark Woodhall

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.