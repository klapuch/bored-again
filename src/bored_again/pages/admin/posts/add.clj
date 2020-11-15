(ns bored-again.pages.admin.posts.add
  (:require [bored-again.pages.layout :as layout])
  (:require [bored-again.models.post :as post])
  (:require [ring.util.response :refer [redirect]])
  (:require [bored-again.pages.forms.post :as post-form]))


(defn- new-post
  [db title text tags]
  (post/new db title text tags))


(defn- form
  [db req]
  (post-form/content db
                     req
                     (fn [values]
                       (let [{id :post_id} (new-post db (:title values) (:text values) (:tags values))]
                         (redirect (format "/posts/%s" id))))))


(defn content
  [db req]
  (let [form-content (form db req)]
    (if (vector? form-content)
      (str
        (layout/admin-template
          [:div.flex.justify-center.mt-10
           form-content]
          "Add"))
      form-content)))
