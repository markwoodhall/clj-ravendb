(ns clj-ravendb.output-coercion
  (:require [clojure.string :refer [join]]
            [clj-ravendb.util :refer [as-seq]]))

(defn escape
  [value]
  (if (= java.lang.String (class value))
    (str \" value \")
    value))

(defn build-from-str
  [from]
  (if (= "docs" from)
    from
    (str "docs." (name from))))

(defn build-where-str
  [where]
  (join " && " (map (fn [w]
                      (let [value (nth w 2)
                            esc-value (escape value)]
                        (str "doc." (name (second w)) (name (first w)) esc-value))) where)))

(defn- to-lambda-or-escape-str
  [prefix ele]
  (if (sequential? ele)
    (let [f (first ele)]
      (if (some #{f} [:Sum :Count])
        (str prefix "." (name f) "(x => x." (name (second ele)) ")")
        (str prefix "." (name f) (if (second ele)
                                   (str "." (name (second ele)))))))
    (escape ele)))

(defn- to-select-str
  [prefix acc ele]
  (str acc
       (if ele
         (if (keyword? ele)
           (str "." (name ele))
           (str "=" (to-lambda-or-escape-str prefix ele))))))

(defn build-select-str
  [select prefix]
  (str "new { " (join "," (map (fn [fields]
                                 (let [f (as-seq fields)
                                       sel-str (reduce
                                                 (partial to-select-str prefix)
                                                 (if (not-empty (filter #(not (keyword? %)) f))
                                                   ""
                                                   prefix)
                                                 f)]
                                   (if (= (first sel-str) \.)
                                     (subs sel-str  1)
                                     sel-str))) select)) " }"))
