(ns bored-again.db.postgres.queries
  (:require [hugsql.core :as hugsql]))

(hugsql/def-db-fns "bored_again/db/postgres/queries.sql")
