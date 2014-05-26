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

(query-index endpoint { :index "TheIndexName" :SomePropertyToQuery "ValueToCheckFor" })

```

## License

Copyright © 2014 Mark Woodhall

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.