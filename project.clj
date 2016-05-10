(defproject clj-ravendb "0.14.0"
  :description "A Clojure library for consuming a RavenDB HTTP api."
  :url "https://github.com/markwoodhall/clj-ravendb"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/mit-license.php"}
  :codox {:metadata {:doc/format :markdown}
          :namespaces [clj-ravendb.rest]
          :source-uri "https://github.com/markwoodhall/clj-ravendb/blob/master/src/{classpath}#L{line}"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.0.1"]
                 [cheshire "5.6.1"]
                 [org.clojure/core.async "0.2.374"]])
