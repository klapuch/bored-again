(ns bored-again.internals.coll)

(def not-empty? (complement empty?))

(defn unique?
  [coll]
  (= (vec (distinct coll)) (vec coll)))
