(ns bored-again.pages.admin.posts.edit
  (:require [bored-again.pages.layout :as layout])
  (:require [bored-again.models.post :as post])
  (:require [ring.util.response :refer [redirect]])
  (:require [bored-again.pages.forms.post :as post-form]))


(defn- edit-post
  [db id title text tags]
  (post/edit db id title text tags))


(defn- form
  [db req id]
  (post-form/content db
                     req
                     (fn [values]
                       (do
                         (edit-post db id (:title values) (:text values) (:tags values))
                         (redirect (format "/admin/posts/%d/edit" id))))
                     (post/by-id db id)))


(defn content
  [db {{id :id} :params :as req}]
  (let [form-content (form db req (Integer. id))]
    (if (vector? form-content)
      (str
        (layout/admin-template
          [:div.flex.justify-center.mt-10
           form-content]
          "Edit"))
      form-content)))
