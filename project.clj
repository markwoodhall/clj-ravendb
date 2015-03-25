(defproject clj-ravendb "0.9.0"
  :description "A Clojure library for consuming a RavenDB HTTP api."
  :url "https://github.com/markwoodhall/clj-ravendb"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/mit-license.php"}
  :plugins [[quickie "0.2.5"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.9.2"]
                 [cheshire "5.4.0"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]
                 [org.clojure/data.json "0.2.3"]]
  :deploy-repositories [
    ["clojars" {:sign-releases false}]
  ]
  :scm {:name "github"
        :url "https://github.com/markwoodhall/clj-ravendb"})
