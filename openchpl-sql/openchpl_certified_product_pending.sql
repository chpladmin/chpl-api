
-- object: openchpl.pending_certified_product | type: TABLE --
DROP TABLE IF EXISTS openchpl.pending_certified_product CASCADE;
CREATE TABLE openchpl.pending_certified_product(
	pending_certified_product_id bigserial NOT NULL,
	
	-- columns from the upload spreadsheet
	unique_id varchar(255),
	record_status varchar(50), --new, update, delete
	practice_type varchar(50), --Inpatient or Ambulatory
	vendor_name varchar(300),
	product_name varchar(300),
	product_version varchar(250),
	certification_edition varchar(10),
	acb_certification_id varchar(250),
	certification_body_name varchar(250),
	product_classification_name varchar(250),
	product_classification_module varchar(260), -- we have no field in the db for this (it is only for Modular EHRs)
	certification_date timestamp,
	vendor_street_address varchar(250), -- not broken out into line1/line2
	vendor_city varchar(250),
	vendor_state varchar(250), -- maps to our region?
	vendor_zip_code varchar(25), -- maps to nothing in our address table
	vendor_website varchar(300),
	vendor_email varchar(250), -- maps to nothing in our vendor table
	additional_software varchar(500),
	upload_notes varchar(500), --maps to nothing in our data model??
	test_report_url varchar(255), -- report_file_location
	
	-- foreign keys that have meaning if they are not mapped
	practice_type_id bigint, -- should never be null
	vendor_id bigint, -- may be null
	vendor_address_id bigint, -- may be null
	product_id bigint, -- may be null
	product_version_id bigint, -- may be null
	certification_edition_id bigint, -- should never be null
	certification_body_id bigint, --should never be null
	product_classification_id bigint, -- should never be null
	CONSTRAINT pending_certified_product_pk PRIMARY KEY (pending_certified_product_id)
);
-- ddl-end --
COMMENT ON TABLE openchpl.pending_certified_product IS 'A product that has been uploaded but not confirmed by the user';
-- ddl-end --
ALTER TABLE openchpl.pending_certified_product OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.pending_certification_criterion | type: TABLE --
DROP TABLE IF EXISTS openchpl.pending_certification_criterion CASCADE;
CREATE TABLE openchpl.pending_certification_criterion(
	pending_certification_criterion_id bigserial NOT NULL,
	certification_criterion_id bigint NOT NULL,
	pending_certified_product_id bigint NOT NULL,
	meets_criteria boolean NOT NULL,
	CONSTRAINT pending_certification_criterion_pk PRIMARY KEY (pending_certification_criterion_id),
	CONSTRAINT certification_criterion_fk FOREIGN KEY (certification_criterion_id)
      REFERENCES certification_criterion (certification_criterion_id) MATCH FULL
      ON UPDATE CASCADE ON DELETE SET NULL,
	CONSTRAINT pending_certified_product_fk FOREIGN KEY (pending_certified_product_id)
      REFERENCES pending_certified_product (pending_certified_product_id) MATCH FULL
      ON UPDATE CASCADE ON DELETE SET NULL
);


-- ddl-end --
COMMENT ON TABLE openchpl.pending_certification_criterion IS 'Criterion that has or has not been met for a pending certified product.';
-- ddl-end --
ALTER TABLE openchpl.pending_certification_criterion OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.pending_cqm_criterion | type: TABLE --
DROP TABLE IF EXISTS openchpl.pending_cqm_criterion CASCADE;
CREATE TABLE openchpl.pending_cqm_criterion(
	pending_cqm_criterion_id bigserial NOT NULL,
	cqm_criterion_id bigint NOT NULL,
	pending_certified_product_id bigint NOT NULL,
	meets_criteria boolean NOT NULL,
	CONSTRAINT pending_cqm_criterion_pk PRIMARY KEY (pending_cqm_criterion_id),
	CONSTRAINT cqm_criterion_fk FOREIGN KEY (cqm_criterion_id)
      REFERENCES cqm_criterion (cqm_criterion_id) MATCH FULL
      ON UPDATE CASCADE ON DELETE SET NULL,
	CONSTRAINT pending_certified_product_fk FOREIGN KEY (pending_certified_product_id)
      REFERENCES pending_certified_product (pending_certified_product_id) MATCH FULL
      ON UPDATE CASCADE ON DELETE SET NULL
);
-- ddl-end --
COMMENT ON TABLE openchpl.pending_cqm_criterion IS 'Criterion that has or has not been met for a pending certified product.';
-- ddl-end --
ALTER TABLE openchpl.pending_cqm_criterion OWNER TO openchpl;
-- ddl-end --