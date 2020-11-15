(ns bored-again.models.tag
  (:require [bored-again.db.postgres.queries :as queries]))

(defn by-id
  [db id]
  (queries/tag-by-id db {:id id}))

(defn tags
  [db]
  (queries/tags db))
