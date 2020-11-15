(ns bored-again.pages.components.posts
  (:require [bored-again.pages.components.tags :refer [h-tags]]))

(defn- h-preview
  [post]
  [:div.mt-4
   (when (:is_top post)
     [:img.inline-block.mr-1.mb-1 {:src "/images/web/star.svg" :width 14 :height 14}])
   [:a.text-xl.text-white.hover:text-gray-500 {:href  (format "/posts/%s-%s" (:id post) (:slug post))
                                               :title (:title post)}
    (:title post)]
   [:span.ml-4
    (h-tags (:tags post))
    [:p.text-xs.text-white
     (.format (java.text.SimpleDateFormat. "d MMMM") (:created_at post))]]])


(defn h-previews
  [groups]
  (for [grouped-posts groups]
    (let [[year posts] grouped-posts]
      (list
        [:h2.text-white.text-3xl year]
        [:hr]
        (map h-preview posts)))))
