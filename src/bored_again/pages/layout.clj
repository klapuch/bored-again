(ns bored-again.pages.layout
  (:require [hiccup2.core :refer [html]])
  (:require [hiccup.page :refer [doctype include-css include-js]]))

(defn template
  ([content description title subtitle is-admin?]
   (html {:mode :html}
         (doctype :html5)
         [:html
          [:head
           [:meta {:http-equiv "content-type" :content "text/html; charset=UTF-8"}]
           (when (some? description)
             [:meta {:name "description" :content (subs description 0 (min 200 (count description)))}])
           (include-css "/css/tailwind.min.css")
           (when is-admin? (include-js "/js/main.js"))
           [:title (when (some? subtitle) (format "%s | " subtitle)) title]]
          [:body.bg-black.font-mono.text-white
           [:div.container.mx-auto.p-5
            [:a {:href "/" :title "BoredAgain"}
             [:img.h-24.w-24.rounded-full.mx-auto {:src "/images/web/avatar.png" :alt "avatar"}]]
            [:ul.flex.items-center.justify-center.mt-2
             [:li
              [:a.text-white.hover:text-gray-500.text-2xl {:href "/posts" :title "Posts"} "Posts"]
              (when is-admin? [:strong [:span.text-red-500.ml-1 "[as admin]"]])]]
            content]]]))
  ([content subtitle description is-admin] (template content description "BoredAgain" subtitle is-admin))
  ([content subtitle description] (template content subtitle description false))
  ([content subtitle] (template content subtitle nil)))


(defn admin-template
  [content subtitle]
  (template content (str subtitle " | Admin") nil true))
