(ns reducers-transducers.core
  (:require
    [clojure.core.reducers :as r])
  (:gen-class))

;; -----REDUCERS-----

;; ----Eager vs Lazy evaluation----
(defn eager-map
  "A dumb map"
  [& args]
  (doall (apply map args)))

(defn eager-filter
  "An eager filter"
  [& args]
  (doall (apply filter args)))

(defn eager-test [nums]
  (eager-filter even? (eager-map inc nums)))

(defn lazy-test [nums]
  (doall (filter even? (map inc nums))))

;; Testing with 5 Billion numbers
#_(time (do (eager-test (range 1 50000000)) nil)) ;; 14985.954254 msecs
#_(time (do (lazy-test  (range 1 50000000)) nil)) ;; 9954.57105 msecs
;; ---------------------------------------

(def data (into [] (take 10000000 (repeatedly #(rand-int 1000)))))


;; ----Reducer definition and working----
(r/reducer (range) (comp (take 9) (partition-all 3)))
(reduce conj (r/reducer (range) (comp (take 9) (partition-all 3))))
;; ---------------------------------------

;; ----r/reduce fn in action----
;; Maps are reduce with reduce-kv and vectors are reduced with CollReduce protocol
(def mp {:a 10 :b 12})

(reduce-kv (fn [m k v]
             (assoc m k (inc v)))
           {} mp)
(r/reduce (fn [m k v]
             (assoc m k (inc v)))
          {} mp)
;; ---------------------------------------

;; ----Testing on 1 Billion numbers----
#_(time (def a (reduce + (map inc (range 1 100000000))))) ;; 11341.426846 msecs
#_(time (def a (r/fold + (r/map inc (range 1 100000000))))) ;; 8442.76924 msecs
#_(time (def a (r/fold (* 16 1024) + + (r/map inc (range 1 100000000))))) ;; 7407.046062 msecs
;; ---------------------------------------

;; ----Testing performance on frequency fn----
(defn frequencies [coll]
  (reduce
    (fn [counts x]
      (merge-with + counts {x 1}))
    {} coll))

(defn pfrequencies [coll]
  (apply merge-with
         +
         (pmap clojure.core/frequencies (partition-all 512 coll))))

(defn p2frequencies [coll]
  (r/fold
    (fn combinef
      ([] {})
      ([x y] (merge-with + x y)))
    (fn reducef
      ([counts x] (merge-with + counts {x 1})))
    coll))

#_(time (do (frequencies data) nil)) ;; 6164.858998 msecs
#_(time (do (pfrequencies data) nil)) ;; 5333.760102 msecs
#_(time (do (p2frequencies data) nil)) ;; 4920.134055 msecs
;; ---------------------------------------

;; ----Basic Funda----
;; 1. Reduce Function
(defn count-words
  ([] {})
  ([freqs word]
   (assoc freqs word (inc (get freqs word 0)))))

;; 2. Combine function
(defn merge-counts
  ([] {})
  ([& m] (apply merge-with + m)))

;; And Fold
(defn word-frequency [text]
  (r/fold merge-counts count-words (clojure.string/split text #"\s+")))
;; ---------------------------------------

;; -----TRANSDUCERS-----

(filter odd?)

(def xf
  (comp
    (filter odd?)
    (map inc)
    (take 5)))
xf
;; xf is equivalent to this
(->> (range)
     (filter odd?)
     (map inc)
     (take 5))

#_((comp str +) 8 8 8)

(transduce xf + (range)) ;; => 30
(transduce xf + 100 (range)) ;; => 130

(def iter
  (eduction xf (range)))
iter

(into [] xf (range))

(sequence xf (range))


;; ----Transducer in action----
;; Much Clojure code relies on applying nested transformations to sequences:
(time (reduce + (filter odd? (map inc (range 100000000))))) ;; 3960.99076 msecs

;; Conceptually, this takes an input sequence of 1000 elements (range 100),
;; then creates an intermediate sequence of (map inc),
;; then creates an intermediate sequence of (filter odd?),
;; then reduces that with +.

;; Transducers let you represent the transformation parts as an independent (reusable) thing:

(def xf
  (comp
    (map inc)
    (filter odd?)))

(def xf (map #(get % "a" 0)))
xf

(time (transduce xf + [{"a" 2} {"a" 5} {"a" 9} {"a" 10}]))
(time (reduce + (mapv #(get % "a") [{"a" 2} {"a" 5} {"a" 9} {"a" 10}])))
;; You then apply that composite transformation in a single pass over the input:
(time (transduce xf + 0 (range 100000000))) ;; 1368.760545 msecs


(time (r/fold + (r/map inc (range 1 10000000))))
(time (transduce (map inc) + (range 1 10000000)))

;; Transducer to find distinct elements
(defn distinct-2
  []
  (fn [rf]
    (let [seen (volatile! #{})]
      (fn
        ([] (rf))              ;; init arity
        ([result] (rf result)) ;; completion arity
        ([result input]      ;; reduction arity
         (if (contains? @seen input)
           result
           (do
             (vswap! seen conj input)
             (rf result input))))))))

(transduce (distinct-2) conj [1 2 1 3 4 3])
