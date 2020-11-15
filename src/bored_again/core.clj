(ns bored-again.core
  (:gen-class)
  (:require [ring.middleware.resource :refer [wrap-resource]])
  (:require [ring.middleware.params :refer [wrap-params]])
  (:require [ring.util.response :refer [resource-response redirect]])
  (:require [compojure.core :refer [defroutes GET POST context]])
  (:require [compojure.route :as route])
  (:require [ring.adapter.jetty :refer [run-jetty]])
  (:require [ring.middleware.reload :refer [wrap-reload]])
  (:require [ring.middleware.basic-authentication :refer [wrap-basic-authentication]])
  (:require [environ.core :refer [env]])
  (:require [bored-again.pages.posts :as posts])
  (:require [bored-again.handlers :as handlers])
  (:require [bored-again.pages.admin.posts.add :as admin-posts->add])
  (:require [bored-again.pages.admin.posts.edit :as admin-posts->edit])
  (:require [bored-again.pages.post :as post])
  (:require [bored-again.pages.root :as root])
  (:require [bored-again.pages.tags :as tags])
  (:require [bored-again.pages.not-found :as not-found]))


(def db {:dbtype (env :db-type)
         :dbname (env :db-name)
         :host (env :db-host)
         :user (env :db-user)
         :password (env :db-password)})


(defn authenticated?
  [name password]
  (and (= name (env :admin-name))
       (= password (env :admin-password))))


(defroutes admin-routes
           (GET "/test" req "abc")
           (GET "/posts/add" req (admin-posts->add/content db req))
           (POST "/posts/add" req (admin-posts->add/content db req))
           (GET "/posts/:id{[1-9]{1}[0-9]{0,9}}/edit" req (admin-posts->edit/content db req))
           (POST "/posts/:id{[1-9]{1}[0-9]{0,9}}/edit" req (admin-posts->edit/content db req)))


(defroutes approutes
           (GET "/" req (root/content))
           (context "/admin" [] (wrap-basic-authentication admin-routes authenticated?))
           (GET "/posts" req (posts/content db))
           (GET "/posts/:id{[1-9]{1}[0-9]{0,9}}" req (post/content db req))
           (GET "/posts/:id{[1-9]{1}[0-9]{0,9}}-:slug{[a-z0-9-]+}" req (post/content db req))
           (GET "/tags/:id{[1-9]{1}[0-9]{0,9}}" req (tags/content db req))
           (GET "/tags/:id{[1-9]{1}[0-9]{0,9}}-:slug{[a-z0-9-]+}" req (tags/content db req))
           (route/not-found (not-found/content)))


(def app
  (-> approutes
      (wrap-reload #'approutes)
      (handlers/wrap-security-headers)
      (wrap-params)
      (wrap-resource "public")))


(defn server
  []
  (run-jetty app {:join? false, :port 3000}))


(defn -main
  [& args]
  (server))
