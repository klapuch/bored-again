(ns bored-again.internals.str
  (:require [clojure.string :as str]))

(defn first-capitalize?
  [text]
  (let [[first-letter] text]
    (= (str first-letter) (str/upper-case first-letter))))

