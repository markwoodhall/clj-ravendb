(defproject clj-ravendb "0.13.0"
  :description "A Clojure library for consuming a RavenDB HTTP api."
  :url "https://github.com/markwoodhall/clj-ravendb"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/mit-license.php"}
  :plugins [[quickie "0.2.5"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "2.1.0"]
                 [cheshire "5.4.0"]
                 [org.clojure/core.async "0.2.374"]])
