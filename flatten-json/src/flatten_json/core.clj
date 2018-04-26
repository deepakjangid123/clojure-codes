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
  "Returns a clojure map, if it finds vectors then maps them with their indices"
  [mp]
  (reduce
    (fn [l [k v]]
      (assoc l k (cond
                   (map? v) (make-map v)
                   (coll? v) (make-map (zipmap (range) v))
                   :else v)))
    {} mp))

(defn flatten-json
  "Flattens the json"
  [prefix separator mp]
  (reduce-kv
    #(let [fld-key (if prefix (str prefix separator %2) %2)]
       (if (map? %3)
         (merge %1 (flatten-json fld-key separator %3))
         (assoc %1 fld-key %3))) {} mp))


(flatten-json nil "_" (make-map (if (vector? data)
                                  (zipmap (range) data)
                                  data)))
