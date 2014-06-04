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
(def results (:results (load-documents endpoint ["employees/1" "employees/2"]))

```

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

The tests for this project assume an instance of RavenDB is running at http://localhost:8080. They also assume that the instance contains the sample northwind database. Available here https://github.com/ayende/ravendb/blob/2.5/Raven.Studio/Assets/EmbeddedData/Northwind.dump
## License

Copyright © 2014 Mark Woodhall

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.