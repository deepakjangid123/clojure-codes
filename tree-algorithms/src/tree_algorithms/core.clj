(ns tree-algorithms.core
  (:require
    [clojure.set :as se])
  (:gen-class))

;; Graph {:vertices '() :egdes '()}
;; Vertices are symbols and edges are triples of [start destination weight]

(def empty-tree {:vertices '() :edges '()})
(def graph {:vertices '(1 2 3 4 5)
            :edges '([1 3 2] [2 3 1] [1 5 10] [3 4 3] [1 2 0.5])})

(defn- add-vertex
  "Adds vertex in the tree"
  [tree vertex]
  (if (nil? vertex)
    tree
    {:vertices (cons vertex (:vertices tree)) :edges (:edges tree)}))

(defn- weight-comparator
  "Compares the weights for two edges and returns true if left edge weight
  is smaller than right one."
  [left right]
  (< (last left) (last right)))

(defn- find-tree-for-vertex-in-forest
  "Finds tree for a vertex in the given forest"
  [v]
  (fn [forest]
    (some
      (fn [t]
        (when (some #(= v %) (:vertices t)) t))
      forest)))

(defn- remove-trees-from-forest
  "Removes trees from forest"
  [& trees]
  (fn [forest]
    (remove (fn [t] (some #(= t %) trees)) forest)))

(defn- merge-trees
  "Merges trees and concat their weighted edges"
  [left right]
  {:vertices (concat (:vertices left) (:vertices right))
   :edges (concat (:edges left) (:edges right))})

(defn- add-branch
  "Adds branch based on the given tree's vertices"
  [tree branch]
  {:vertices (:vertices tree)
   :edges (cons branch (:edges tree))})

(defn minimum-spanning-tree-by-kruskal
  "Given a connected weighted undirected graph this function returns
  the minimum spanning tree determined by applying Kruskal's algorithm.
  Essentially this involves putting every vertex of the graph into it's own
  tree, and then joining up the trees using the smallest edge between them.
  Any edges that start and end in the same tree are discarded.
  It's complete when all of the edges have been used or discarded."
  [{vertices :vertices, edges :edges}]
  (loop [sorted-edges (sort weight-comparator edges)
         vertex-trees (map (partial add-vertex empty-tree) vertices)]
    (if (empty? sorted-edges) ;; Base condition
      (or (first vertex-trees) empty-tree)
      (let [[[u v d] & to-walk] sorted-edges
            tree-with-u ((find-tree-for-vertex-in-forest u) vertex-trees)
            tree-with-v ((find-tree-for-vertex-in-forest v) vertex-trees)]
        (if (= tree-with-u tree-with-v)
          (recur to-walk vertex-trees)
          (recur
            to-walk
            (conj
              ((remove-trees-from-forest tree-with-u tree-with-v) vertex-trees)
              (add-branch (merge-trees tree-with-u tree-with-v) [u v d]))))))))

#_(minimum-spanning-tree-by-kruskal graph)

(defn- edge-out-of
  "Looks if we have to add u or v as vertex.
  Whichever is already present in vertices, will not be added again"
  [{vertices :vertices}]
  (let [out-of (fn [u v]
                 (and (some #(= u %) vertices) (not (some #(= v %) vertices))))]
    (fn [[u v d]]
      (cond
        (out-of u v) [v [u v d]]
        (out-of v u) [u [u v d]]))))

(defn minimum-spanning-tree-by-prim
  "Given a connected weighted undirected graph this function returns
  the minimum spanning tree determined by applying Prim's algorithm.
  The algorithm is pretty simple:
  1). Pick any vertex to start from, then find the shortest edge from it to
  another vertex and add that to your tree.
  2). Repeat this process of picking the shortest edge from any of the vertices
  in the tree being built.
  3). When all vertices from the original graph are in the tree you have
  the minimum spanning tree."
  [{vertices :vertices, edges :edges}]
  (loop [sorted-edges (sort weight-comparator edges)
         current-tree (add-vertex empty-tree (first vertices))]
    (if (empty? sorted-edges) ;; Base condition
      current-tree
      (if (empty? (se/difference (set vertices) (set (:vertices current-tree))))
        current-tree
        (let [[vertex edge] (some (edge-out-of current-tree) sorted-edges)]
          (recur
            (remove #(= edge %) sorted-edges)
            (add-branch (add-vertex current-tree vertex) edge)))))))

#_(minimum-spanning-tree-by-prim graph)

;; ---------------
;; BST
;; ---------------
(defrecord Node [element left right])

(defn <min>
  "Gives smallest element from the tree"
  [{:keys [element left]}]
  (if left
    (recur left)
    element))

(defn <max>
  "Returns largest element from the tree"
  [{:keys [element right]}]
  (if right
    (recur right)
    element))

(defn <contains?>
  "Search for an element in the tree"
  [{:keys [element left right] :as tree} value]
  (cond
   (nil? tree) false
   (< value element) (recur left value)
   (> value element) (recur right value)
   :else true))

(defn <count>
  "Counts nodes of the tree"
  [{:keys [left right] :as tree}]
  (if tree
    (+ 1 (<count> left) (<count> right))
    0))

(defn height
  "Return height of the tree"
  ([tree] (height tree 0))
  ([tree no-of-nodes]
   (if tree
     (max (height (:left tree) (inc no-of-nodes))
          (height (:right tree) (inc no-of-nodes)))
     no-of-nodes)))

(defn insert
  "Inserts node in the tree"
  [{:keys [element left right] :as tree} value]
  (cond
   (nil? tree) (Node. value nil nil)
   (< value element) (Node. element (insert left value) right)
   (> value element) (Node. element left (insert right value))
   :else tree))

(defn remove
  "Removes node from a tree"
  [{:keys [element left right] :as tree} value]
  (cond
   (nil? tree) nil
   (< value element) (Node. element (remove left value) right)
   (> value element) (Node. element left (remove right value))
   (nil? left) right
   (nil? right) left
   :else (let [min-value (<min> right)]
           (Node. min-value left (remove right min-value)))))

(defn bst?
  "Returns true if given tree is BST, within given range"
  ([tree] (bst? tree Integer/MIN_VALUE Integer/MAX_VALUE))
  ([{:keys [element left right] :as tree} minimum maximum]
   (cond
     (nil? tree) true
     (or (< element minimum) (> element maximum)) false
     :else (and (bst? left minimum (dec element))
                (bst? right (inc element) maximum)))))

(def to-tree #(reduce insert nil %))

(defn to-list
  "Returns list in inorder fashion"
  [{:keys [element left right] :as tree}]
  (when tree
    `(~@(to-list left) ~element ~@(to-list right))))

(def tree (to-tree '(5 8 2 3 4 1)))

tree
(bst? tree) ; true
(<count> tree) ; 6
(height tree) ; 4
(<max> tree) ; 8
(<min> tree) ; 1
(to-list (remove tree 3)) ; (1 2 4 5 8)
(<contains?> tree 2) ; true
