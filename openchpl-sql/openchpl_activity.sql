CREATE TABLE openchpl.activity
(
   activity_id bigserial NOT NULL, 
   description character varying(1000), 
   activity_date timestamp without time zone NOT NULL DEFAULT now(), 
   activity_object_id bigint NOT NULL, 
   activity_object_concept_id bigint NOT NULL, 
   creation_date timestamp without time zone NOT NULL DEFAULT now(), 
   last_modified_date timestamp without time zone NOT NULL DEFAULT now(), 
   last_modified_user bigint NOT NULL, 
   deleted boolean NOT NULL DEFAULT false, 
   CONSTRAINT activity_id_pk PRIMARY KEY (activity_id)
) 
WITH (
  OIDS = FALSE
)
;
ALTER TABLE openchpl.activity
  OWNER TO openchpl;


  
CREATE TABLE openchpl.activity_concept
(
  activity_concept_id bigserial NOT NULL,
  concept character varying,
  creation_date timestamp without time zone NOT NULL DEFAULT now(),
  last_modified_date timestamp without time zone NOT NULL DEFAULT now(),
  last_modified_user bigint NOT NULL,
  deleted boolean NOT NULL DEFAULT false,
  CONSTRAINT activity_concept_id_pk PRIMARY KEY (activity_concept_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE openchpl.activity_concept
  OWNER TO openchpl;

  
 
ALTER TABLE openchpl.activity ADD CONSTRAINT activity_object_concept_fk FOREIGN KEY (activity_object_concept_id) REFERENCES openchpl.activity_concept (activity_concept_id);
