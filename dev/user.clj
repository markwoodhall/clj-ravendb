(ns user
  (:require
   [clojure.tools.namespace.repl :refer :all]))

(when (System/getProperty "clj-ravendb.load_nrepl")
  (require 'nrepl))
