(ns bored-again.pages.tags
  (:require [bored-again.models.post :as post])
  (:require [bored-again.models.tag :as tag])
  (:require [bored-again.pages.layout :as layout])
  (:require [ring.util.response :refer [redirect]])
  (:require [bored-again.pages.components.posts :refer [h-previews]]))

(defn- h-not-found-tag
  []
  [:h1.text-4xl.text-center.text-white "This tag doesn't exist."])


(defn content
  [db {{id :id slug :slug} :params :as req}]
  (let [posts (post/by-tag-id db (Integer. id))]
    (if (empty? posts)
      {:status 404
       :body   (str
                 (layout/template
                   (h-not-found-tag)
                   "Tag not found."))}
      (let [{origin-name :name origin-slug :slug} (tag/by-id db (Integer. id))]
        (if (not= slug origin-slug)
          (redirect (format "/tags/%s-%s" id origin-slug))
          (str
            (layout/template
              (list
                [:h1.text-4xl.text-white (str "#" origin-name)]
                (h-previews posts))
              (format "#%s posts" origin-name)
              (format "Posts by tag #%s." origin-name))))))))
