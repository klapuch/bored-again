-- :name post-by-id :? :*
SELECT
	p.id,
	p.title,
	p.slug,
	p.text,
	p.created_at,
	t.name AS tag_name,
	t.slug AS tag_slug,
	t.id AS tag_id
FROM posts AS p
LEFT JOIN post_tags AS pt ON p.id = pt.post_id
LEFT JOIN tags AS t ON t.id = pt.tag_id
WHERE p.id = :id
ORDER BY pt.position;


-- :name posts-by-most-recent :? :*
SELECT
	p.id,
	p.title,
	p.slug,
	p.text,
	p.created_at,
	extract(year from p.created_at)::integer AS year,
	t.name AS tag_name,
	t.slug AS tag_slug,
	t.id AS tag_id,
	row_number() OVER (PARTITION BY extract(year from p.created_at) ORDER BY mvp.position ASC, p.created_at DESC, p.id DESC) <= constant.max_top_posts() AS is_top
FROM posts AS p
LEFT JOIN post_tags AS pt ON p.id = pt.post_id
LEFT JOIN tags AS t ON t.id = pt.tag_id
LEFT JOIN most_visited_posts AS mvp ON mvp.post_id = p.id
ORDER BY created_at DESC, pt.position;


-- :name posts-by-tag-id :? :*
SELECT
	p.id,
	p.title,
	p.slug,
	p.text,
	p.created_at,
	extract(year from p.created_at)::integer AS year,
	t.name AS tag_name,
	t.slug AS tag_slug,
	t.id AS tag_id,
	tp.is_top
FROM posts AS p
LEFT JOIN post_tags AS pt ON p.id = pt.post_id
LEFT JOIN tags AS t ON t.id = pt.tag_id
LEFT JOIN (
	SELECT p.id, row_number() OVER (PARTITION BY extract(year from p.created_at) ORDER BY mvp.position ASC, p.created_at DESC, p.id DESC) <= constant.max_top_posts() AS is_top
	FROM posts AS p
	LEFT JOIN most_visited_posts AS mvp ON mvp.post_id = p.id
) AS tp ON tp.id = p.id
WHERE p.id IN (
	SELECT post_id
	FROM post_tags
	WHERE tag_id = :tag_id
)
ORDER BY array_position(ARRAY[:tag_id]::integer[], pt.tag_id), p.created_at DESC;


-- :name tag-by-id :? :1
SELECT slug, name
FROM tags
WHERE id = :id;


-- :name tags :? :*
SELECT id, slug, name
FROM tags
ORDER BY last_used_at DESC NULLS LAST, name ASC;


-- :name insert-new-post :<! :1
WITH inserted_tags AS (
    INSERT INTO tags (name) VALUES :tuple*:tags
    ON CONFLICT (lower(name)) DO UPDATE SET last_used_at = CURRENT_TIMESTAMP
    RETURNING id
), inserted_post AS (
    INSERT INTO posts (title, text) VALUES (:title, :text)
    RETURNING id
)
INSERT INTO post_tags (post_id, tag_id, position)
SELECT ip.id, it.id, row_number() OVER ()
FROM inserted_post AS ip, inserted_tags AS it
RETURNING post_id;


-- :name update-post :!
WITH inserted_tags AS (
    INSERT INTO tags (name) VALUES :tuple*:tags
    ON CONFLICT (lower(name)) DO UPDATE SET last_used_at = CURRENT_TIMESTAMP
    RETURNING id
), updated_post AS (
    UPDATE posts
    SET title = :title,
        text = :text
    WHERE id = :post_id
    RETURNING id
), deleted_post_tags AS (
    DELETE FROM post_tags
    WHERE post_id = :post_id
    AND tag_id NOT IN (SELECT id FROM inserted_tags)
)
INSERT INTO post_tags (post_id, tag_id, position)
SELECT up.id, it.id, row_number() OVER ()
FROM updated_post AS up, inserted_tags AS it
ON CONFLICT (post_id, tag_id) DO NOTHING;
