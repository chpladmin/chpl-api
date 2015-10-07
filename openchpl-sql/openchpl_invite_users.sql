-- object: openchpl.invited_user | type: TABLE --
--DROP TABLE IF EXISTS openchpl.invited_user CASCADE;
CREATE TABLE openchpl.invited_user(
	invited_user_id bigserial NOT NULL,
	email varchar(300) NOT NULL,
	certification_body_id bigint,
	token varchar(500) NOT NULL,

	-- fields we need for auditing/tracking
	creation_date timestamp without time zone NOT NULL DEFAULT now(),
	last_modified_date timestamp without time zone NOT NULL DEFAULT now(),
	last_modified_user bigint NOT NULL,
	deleted boolean NOT NULL DEFAULT false,
	CONSTRAINT invited_user_pk PRIMARY KEY (invited_user_id),
	CONSTRAINT token_unique UNIQUE (token)
);
-- ddl-end --
COMMENT ON TABLE openchpl.invited_user IS 'A user that has been invited to use the CHPL system.';
-- ddl-end --
ALTER TABLE openchpl.invited_user
 OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.invited_user_permission | type: TABLE --
--DROP TABLE IF EXISTS openchpl.invited_user_permission CASCADE;
CREATE TABLE openchpl.invited_user_permission(
	invited_user_permission_id bigserial NOT NULL,
	invited_user_id bigint NOT NULL,
	user_permission_id bigint NOT NULL,

	-- fields we need for auditing/tracking
	creation_date timestamp without time zone NOT NULL DEFAULT now(),
	last_modified_date timestamp without time zone NOT NULL DEFAULT now(),
	last_modified_user bigint NOT NULL,
	deleted boolean NOT NULL DEFAULT false,
	CONSTRAINT invited_user_permission_fk PRIMARY KEY (invited_user_permission_id),
	CONSTRAINT permission_unique UNIQUE (invited_user_id, user_permission_id)
);
-- ddl-end --
ALTER TABLE openchpl.invited_user_permission ADD CONSTRAINT invited_user_id_fk FOREIGN KEY (invited_user_id)
REFERENCES openchpl.invited_user (invited_user_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

ALTER TABLE openchpl.invited_user_permission ADD CONSTRAINT user_permission_id_fk FOREIGN KEY (user_permission_id)
REFERENCES openchpl.user_permission (user_permission_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;

COMMENT ON TABLE openchpl.invited_user_permission IS 'A user that has been invited to use the CHPL system.';
-- ddl-end --
ALTER TABLE openchpl.invited_user_permission
 OWNER TO openchpl;
-- ddl-end --