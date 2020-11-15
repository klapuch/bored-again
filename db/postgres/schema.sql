CREATE SCHEMA constructs;
CREATE DOMAIN constructs.text_not_empty AS text CHECK (trim(VALUE) != '');

CREATE SCHEMA constant;
CREATE FUNCTION constant.post_tags_max() RETURNS integer AS $$SELECT 7;$$ LANGUAGE sql IMMUTABLE;
CREATE FUNCTION constant.max_top_posts() RETURNS integer AS $$SELECT 3;$$ LANGUAGE sql IMMUTABLE;
CREATE FUNCTION constant.post_title_max_length() RETURNS integer AS $$SELECT 150;$$ LANGUAGE sql IMMUTABLE;

CREATE TABLE transliterate_to_ascii_rules (
	original text NOT NULL PRIMARY KEY,
	transliterate text
);

CREATE FUNCTION transliterate_to_ascii(in_text text) RETURNS text AS $BODY$
DECLARE
	r record;
BEGIN
	FOR r IN SELECT original, transliterate FROM transliterate_to_ascii_rules WHERE original IN (
		SELECT char
		FROM (SELECT unnest(regexp_split_to_array($1, '')) AS char) AS matches
		WHERE matches.char NOT SIMILAR TO '[a-zA-Z1-9]'
		GROUP BY matches.char
	)
	LOOP
		in_text = replace(in_text, r.original, r.transliterate);
	END LOOP;

  RETURN trim(in_text);
END;
$BODY$ LANGUAGE plpgsql STABLE;

CREATE FUNCTION string_webalize(text) RETURNS text AS $BODY$
BEGIN
	RETURN trim(BOTH '-' FROM regexp_replace(lower(transliterate_to_ascii(translate($1, '@°', 'a '))), '[^a-z0-9]+', '-', 'g'));
END;
$BODY$ LANGUAGE plpgsql IMMUTABLE;


CREATE TABLE posts (
	id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	title constructs.text_not_empty NOT NULL,
	text constructs.text_not_empty NOT NULL,
	created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
	slug text NOT NULL GENERATED ALWAYS AS (string_webalize(title)) STORED,
	CONSTRAINT title_max_length CHECK (length(title) <= constant.post_title_max_length())
);

CREATE FUNCTION posts_trigger_row_biu() RETURNS trigger AS $BODY$
BEGIN
	new.text = trim(new.text);
	new.title = trim(new.title);

	RETURN new;
END;
$BODY$ LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER posts_row_biu_trigger
	BEFORE INSERT OR UPDATE
	ON posts
	FOR EACH ROW EXECUTE PROCEDURE posts_trigger_row_biu();


CREATE TABLE tags (
	id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	name constructs.text_not_empty NOT NULL,
	created_at timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_used_at timestamp with time zone,
	slug text NOT NULL GENERATED ALWAYS AS (string_webalize(name)) STORED
);

CREATE UNIQUE INDEX tags_name_ukey ON tags (lower(name));

CREATE FUNCTION tags_trigger_row_biu() RETURNS trigger AS $BODY$
BEGIN
	new.name = trim(new.name);

	RETURN new;
END;
$BODY$ LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER tags_row_biu_trigger
	BEFORE INSERT OR UPDATE
	ON tags
	FOR EACH ROW EXECUTE PROCEDURE tags_trigger_row_biu();


CREATE TABLE post_tags (
	id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	post_id integer NOT NULL REFERENCES posts (id) ON DELETE CASCADE ON UPDATE RESTRICT,
	tag_id integer NOT NULL REFERENCES tags (id) ON DELETE CASCADE ON UPDATE RESTRICT,
	position smallint NOT NULL,
	CONSTRAINT position_min CHECK (position >= 1)
);

CREATE UNIQUE INDEX post_tags_post_tag_id_ukey ON post_tags (post_id, tag_id);
CREATE UNIQUE INDEX post_tags_post_tag_id_position_ukey ON post_tags (post_id, tag_id, position);

CREATE FUNCTION post_tags_trigger_row_biu() RETURNS trigger AS $BODY$
BEGIN
	IF old.post_id IS DISTINCT FROM new.post_id AND EXISTS (SELECT 1 FROM post_tags WHERE post_id = new.post_id GROUP BY tag_id HAVING count(*) > constant.post_tags_max()) THEN
		RAISE EXCEPTION USING
			MESSAGE = format('Post "%s" can have max %s tags.', new.post_id, constant.post_tags_max());
	END IF;

	RETURN new;
END;
$BODY$ LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER post_tags_row_biu_trigger
	BEFORE INSERT OR UPDATE
	ON post_tags
	FOR EACH ROW EXECUTE PROCEDURE post_tags_trigger_row_biu();


CREATE FUNCTION post_tags_trigger_row_aud() RETURNS trigger AS $BODY$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM post_tags WHERE tag_id = old.tag_id) THEN
        DELETE FROM tags
        WHERE id = old.tag_id;
    END IF;

	RETURN new;
END;
$BODY$ LANGUAGE plpgsql VOLATILE;


CREATE TRIGGER post_tags_row_aud_trigger
	AFTER UPDATE OR DELETE
	ON post_tags
	FOR EACH ROW EXECUTE PROCEDURE post_tags_trigger_row_aud();


CREATE TABLE post_visits (
	id integer GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
	post_id integer NOT NULL REFERENCES posts (id) ON DELETE CASCADE ON UPDATE RESTRICT,
	period date NOT NULL,
	visits integer NOT NULL DEFAULT 0,
	CONSTRAINT visits_min CHECK (visits >= 0),
	CONSTRAINT period_min CHECK (period <= CURRENT_DATE)
);

CREATE UNIQUE INDEX post_visits_post_id_period_ukey ON post_visits (post_id, period);

CREATE FUNCTION post_visits_trigger_row_bu() RETURNS trigger AS $BODY$
BEGIN
	IF ROW(old.post_id, old.period, old.visits) IS DISTINCT FROM ROW(new.post_id, new.period, new.visits) THEN
		RAISE EXCEPTION USING
			MESSAGE = 'You can not change already counted record.',
			HINT = 'Disable trigger and try again.';
	END IF;

	RETURN new;
END;
$BODY$ LANGUAGE plpgsql VOLATILE;

CREATE TRIGGER post_visits_row_bu_trigger
	BEFORE UPDATE
	ON post_visits
	FOR EACH ROW EXECUTE PROCEDURE post_visits_trigger_row_bu();


CREATE MATERIALIZED VIEW most_visited_posts AS
	SELECT
		pv.post_id,
		row_number() OVER (PARTITION BY pv.post_id ORDER BY SUM(pv.visits) DESC, pv.post_id DESC) AS position
	FROM post_visits AS pv
	GROUP BY pv.post_id;

CREATE UNIQUE INDEX most_visited_posts_post_id_ukey ON most_visited_posts (post_id);
