(ns graph.core
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [ubergraph.core :as uber]
            [ubergraph.alg :as alg])
  (:gen-class))

(defn read-file
  "Reads file and provide line separated output"
  [filepath]
  (s/split-lines (slurp filepath)))

(defn divide-input
  "Divides input based on their lengths in file"
  [input]
  (let [[test-cases & input] input
        input (vec input)]
    (loop [result []
           in input]
      (if (empty? in)
        result
        (recur (conj result (subvec in 1 (inc (* 2 (read-string (first in))))))
               (subvec in (inc (* 2 (read-string (first in))))))))))

(defn make-partition
  "Partition the input like src-dest pair"
  [input]
  (mapv (fn [a]
          (map vec a))
        (mapv #(partition 2 %) input)))

(defn get-input
  "Gets the input in desired form to create graph"
  [filepath]
  (make-partition
    (divide-input
      (read-file filepath))))

(defn create-graph
  "Creates directed edges graph and returns it"
  [input]
  (reduce #(uber/add-directed-edges %1 %2)
          (uber/graph) input))

(defn post-traversal-reverse
  "Creates graph and Returns traversed path, since here only one connected path is there,
  So it will return the longest path and then reverse it"
  [src-dest-pair]
  (-> (create-graph src-dest-pair)
      (alg/post-traverse)))

(defn run-on-small-input
  "Run on small data file from resources"
  []
  (map post-traversal (get-input "resources/C-small-practice.in")))

(defn run-on-large-input
  "Run on large data file from resources"
  []
  (map post-traversal (get-input "resources/C-large-practice.in")))

(defn join-list
  "Joins list with hyphen and space, for ex. (1 2 3) -> '1-2 2-3'"
  [lst]
  (loop [i 0
         result ""]
    (if (>= i (dec (count lst)))
      result
      (recur (inc i) (str result " " (nth lst i) "-" (nth lst (inc i)))))))

(defn write-result-to-file
  [filename result]
  (if (.exists (io/file filename))
    (io/delete-file filename))
  (map #(spit filename (str "Case #" %1 ": " (s/trim (join-list %2)) "\n")
              :append true)
       (iterate inc 1) result))

(def small-file-output (map reverse (run-on-small-input)))
(def large-file-output (map reverse (run-on-large-input)))

(write-result-to-file "small-output.txt" small-file-output)
(write-result-to-file "large-output.txt" large-file-output)
