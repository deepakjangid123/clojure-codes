(ns flatten-json.core
  (:gen-class))


(def data [{"studentName" "Foo" "Age" "12"
            "subjects" [{"name" "English" "marks" "40"}
                        {"name" "History" "marks" "50"}]}
           {"studentName" "Bar" "Age" "12"
            "subjects" [{"name" "English" "marks" "40"}
                        {"name" "History" "marks" "50"}
                        {"name" "Science" "marks" "40"}]}
           {"studentName" "Baz" "Age" "12" "subjects" []}])

(defn make-map
  "Returns a map by zipmapping vectors with natural numbers as keys"
  [mp]
  (reduce
    (fn [l [k v]]
      (assoc l k (cond
                   (map? v) (make-map v)
                   (coll? v) (make-map (zipmap (range) v))
                   :else v)))
    {} mp))

(defn flatten-map
  "Flattens the map and nested maps, if any"
  [prefix separator mp]
  (reduce-kv
    #(let [fld-key (if prefix (str prefix separator %2) %2)]
       (if (map? %3)
         (merge %1 (flatten-map fld-key separator %3))
         (assoc %1 fld-key %3))) {} mp))


(flatten-map nil "_" (make-map (if (coll? data)
                                 (zipmap (range) data)
                                 data)))
