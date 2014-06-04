# raven-clj

A clojure library designed to consume a RavenDB rest api. 

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

Returns a sequence of maps like:

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
---

Putting a document:

```
#!clojure

(put-document endpoint "NewDocumentKey" { :name "TestDoc" :value 1 })

```

Querying an index:

```
#!clojure

(def results (query-index endpoint { :index "TheIndexName" :SomePropertyToQuery "ValueToCheckFor" }))

```

The tests for this project assume an instance of RavenDB is running at http://localhost:8080. They also assume that the instance contains the sample northwind database. The sample northwind database is available [here](https://github.com/ayende/ravendb/blob/2.5/Raven.Studio/Assets/EmbeddedData/Northwind.dump)

## License

Copyright © 2014 Mark Woodhall

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.