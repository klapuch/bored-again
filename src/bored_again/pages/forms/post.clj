(ns bored-again.pages.forms.post
  (:require [bored-again.internals.str :refer [first-capitalize?]])
  (:require [bored-again.internals.coll :as coll])
  (:require [clojure.string :as str])
  (:require [bored-again.pages.components.tags :refer [h-tags]])
  (:require [bored-again.models.tag :refer [tags]]))

(def ^{:private true, :const true} TAGS-DELIMITER #",")
(def ^{:private true, :const true} TAGS-PER-POST 7)
(def ^{:private true, :const true} MAX-TITLE-LENGTH 150)


(defn- split-tags
  [tags]
  (str/split tags TAGS-DELIMITER))


(defn- join-tags
  [tags]
  (when (not= 0 (count tags)) (str/join (str TAGS-DELIMITER) tags)))


(defn- form-valid?
  [& errors]
  (every? nil? errors))


(defn- h-error-text
  [text]
  (when (some? text) [:p.text-red-500.text-xs.italic.mt-1 text]))


(defn- validate-title
  [title]
  (cond
    (str/blank? title) "Title must be filled."
    (> (count title) MAX-TITLE-LENGTH) (format "Title can not longer than %d characters." MAX-TITLE-LENGTH)
    (not (first-capitalize? title)) "Title must starts with capitalize letter."))


(defn- validate-text
  [text]
  (cond
    (str/blank? text) "Text must be filled."))


(defn- validate-tags
  [tags]
  (cond
    (str/blank? tags) "Add at least one tag."
    (not (coll/unique? (split-tags tags))) "All the tags must be unique."
    (= (str (last (str/trim tags))) (str TAGS-DELIMITER)) "Tags can not end with trailing comma."
    (> (count (split-tags tags)) TAGS-PER-POST) (format "There can be only %d tags per post." TAGS-PER-POST)))


(defn content
  ([db req on-success defaults]
    (let [method (:request-method req)
          title-value (get-in req [:form-params "title"] (:title defaults))
          text-value (get-in req [:form-params "text"] (:text defaults))
          tags-value (get-in req [:form-params "tags"] (join-tags (map :name (:tags defaults))))
          title-error (when (= method :post) (validate-title title-value))
          text-error (when (= method :post) (validate-text text-value))
          tags-error (when (= method :post) (validate-tags tags-value))]
      (if (and
            (= method :post)
            (form-valid? title-error text-error tags-error))
        (on-success {:title title-value :text text-value :tags (split-tags tags-value)})
        [:form {:method "POST" :action ""}
         [:div.mb-4
          [:input.block-inline.border.border-grey-400.rounded.px-2.py-1.w-full {:type "text"
                                                                                :name "title"
                                                                                :value title-value
                                                                                :max-length MAX-TITLE-LENGTH
                                                                                :required true
                                                                                :placeholder "Title"}]
          (h-error-text title-error)]
         [:div.mb-4
          [:input#tags.block-inline.border.border-grey-400.rounded.px-2.py-1.w-full {:type "text"
                                                                                     :name "tags"
                                                                                     :value tags-value
                                                                                     :required true
                                                                                     :placeholder "Tags"}]
          (h-error-text tags-error)
          [:div.mt-4
           (h-tags (tags db) (constantly "#"))]]
         [:div.mb-4
          [:textarea.block-inline.border.border-grey-400.rounded.py-1.px-2.w-full {:rows 30
                                                                                   :cols 80
                                                                                   :name "text"
                                                                                   :required true
                                                                                   :placeholder "Text"}
           text-value]
          (h-error-text text-error)]
         [:div.mb-4
          [:button.bg-blue-500.hover:bg-blue-700.text-white.font-bold.py-2.px-4.rounded {:type "submit"
                                                                                         :name "submit"}
           "Post"]]])))
   ([db req on-success] (content db req on-success nil)))
