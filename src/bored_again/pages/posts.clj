(ns bored-again.pages.posts
  (:require [bored-again.models.post :as post])
  (:require [bored-again.pages.layout :as layout])
  (:require [bored-again.pages.components.posts :refer [h-previews]]))


(defn content
  [db]
  (let [posts (post/most-recents db)]
    (str
      (layout/template
        (h-previews posts)
        "Posts"
        "The most recent posts."))))
