(ns bored-again.pages.not-found
  (:require [bored-again.pages.layout :as layout]))

(defn content
  []
  (str
    (layout/template
      [:h1.text-4xl.text-center.text-white "This page doesn't exist."]
      "404 Not Found")))
