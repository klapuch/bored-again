(ns bored-again.models.post
  (:require [clojure.set :as set])
  (:require [bored-again.db.postgres.queries :as queries])
  (:require [bored-again.internals.coll :as coll]))

(def ^{:private true, :const true} tag-columns-mapping {:tag_name :name :tag_id :id :tag_slug :slug})

(defn- tags
  [grouped-posts]
  (->> grouped-posts
       (map #(select-keys % (keys tag-columns-mapping)))
       (map #(set/rename-keys % tag-columns-mapping))))


(defn- with-tags
  ([post tags]
   (when (coll/not-empty? post)
     (-> (assoc post :tags (when (some :id tags) tags))
         (dissoc :tag_name :tag_id :tag_slug))))
  ([posts]
   (->> posts
        (group-by :id)
        (map second)
        (map #(with-tags (first %) (tags %))))))


(defn by-id
  [db id]
  (let [post (queries/post-by-id db {:id id})]
    (with-tags (first post) (tags post))))


(defn most-recents
  [db]
  (->> (queries/posts-by-most-recent db)
       (with-tags)
       (group-by :year)))


(defn by-tag-id
  [db tag-id]
  (->> (queries/posts-by-tag-id db {:tag_id tag-id})
       (with-tags)
       (group-by :year)))


(defn new
  [db title text tags]
  (queries/insert-new-post db {:title title :text text :tags (vec (map vector tags))}))


(defn edit
  [db id title text tags]
  (queries/update-post db {:post_id id :title title :text text :tags (vec (map vector tags))}))
