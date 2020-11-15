(ns bored-again.pages.post
  (:require [ring.util.response :refer [redirect]])
  (:require [hiccup.util :refer [raw-string]])
  (:require [bored-again.pages.layout :as layout])
  (:require [bored-again.models.post :as post])
  (:require [bored-again.pages.components.tags :refer [h-tags]]))


(defn- h-not-found-post
  []
  [:h1.text-4xl.text-center.text-white "This post doesn't exist."])


(defn- h-post
  [post]
  (list
    [:h1.text-4xl.text-white (:title post)]
    (when-some [tags (:tags post)]
      [:div.pb-4 (h-tags tags)])
    [:p.text-justify.text-lg.text-gray-400 (raw-string (:text post))]
    [:br]
    [:p.text-sm.text-white
     (.format (java.text.SimpleDateFormat. "d MMMM YYYY") (:created_at post))]))


(defn content
  [db {{id :id slug :slug} :params :as req}]
  (let [post (post/by-id db (Integer. id))]
    (if (empty? post)
      {:status 404
       :body   (str
                 (layout/template
                   (h-not-found-post)
                   "Post not found."))}
      (if (not= slug (:slug post))
        (redirect (format "/posts/%s-%s" id (:slug post)))
        (str
          (layout/template
            (h-post post)
            (:title post)
            (:text post)))))))
