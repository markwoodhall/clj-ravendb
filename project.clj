(defproject clj-ravendb "0.2.0"
  :description "A clojure library for consuming a RavenDB rest api."
  :url "https://bitbucket.org/markwoodhall/clj-ravendb"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[quickie "0.2.5"]]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [clj-http "0.9.2"]
                 [org.clojure/core.async "0.1.303.0-886421-alpha"]                
                 [org.clojure/data.json "0.2.3"]]
  :deploy-repositories [
    ["clojars" {:sign-releases false}]
  ]
  :scm {:name "bitbucket"
        :url "https://bitbucket.org/markwoodhall/clj-ravendb"})
