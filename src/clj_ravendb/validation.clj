(ns clj-ravendb.validation)

(defn- nilify-invalid-operations
  [{:keys [method document metadata key]}]
  (cond
    (= method "PUT") (and document metadata key)
    (= method "DELETE") key))

(defn validate-bulk-operations
  "Validates a given sequence of bulk operations to apply. 
  In RavenDB land these are usually PUT and DELETE requests."
  [operations]
  {:pre [(not-empty operations)]}
  (let [operation-count (count operations)
        valid-operations (filter (comp not nil?) (map nilify-invalid-operations operations))
        valid-operation-count (count valid-operations)]
  (= operation-count valid-operation-count)))

