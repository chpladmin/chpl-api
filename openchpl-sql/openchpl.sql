-- Database generated with pgModeler (PostgreSQL Database Modeler).
-- pgModeler  version: 0.8.0
-- PostgreSQL version: 9.4
-- Project Site: pgmodeler.com.br
-- Model Author: ---


-- Database creation must be done outside an multicommand file.
-- These commands were put in this file only for convenience.
-- -- object: openchpl | type: DATABASE --
-- -- DROP DATABASE IF EXISTS openchpl;
-- CREATE DATABASE openchpl
-- ;
-- -- ddl-end --
-- 

-- object: openchpl | type: SCHEMA --
-- DROP SCHEMA IF EXISTS openchpl CASCADE;
CREATE SCHEMA openchpl;
-- ddl-end --
ALTER SCHEMA openchpl OWNER TO openchpl;
-- ddl-end --

SET search_path TO pg_catalog,public,openchpl;
-- ddl-end --

-- object: openchpl.user | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.user CASCADE;
CREATE TABLE openchpl.user(
	user_id bigserial NOT NULL,
	user_name varchar(25) NOT NULL,
	password varchar(255) NOT NULL,
	account_expired bool NOT NULL,
	account_locked bool NOT NULL,
	credentials_expired bool NOT NULL,
	account_enabled bool NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL,
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT user_pk PRIMARY KEY (user_id)

);
-- ddl-end --
ALTER TABLE openchpl.user OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.certification_body | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.certification_body CASCADE;
CREATE TABLE openchpl.certification_body(
	certification_body_id bigserial NOT NULL,
	address_id bigint,
	name varchar(250),
	website varchar(300),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user smallint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT certification_body_pk PRIMARY KEY (certification_body_id)

);
-- ddl-end --
ALTER TABLE openchpl.certification_body OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.product | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.product CASCADE;
CREATE TABLE openchpl.product(
	product_id bigserial NOT NULL,
	vendor_id bigint NOT NULL,
	name varchar(300) NOT NULL,
	report_file_location varchar(255),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT product_pk PRIMARY KEY (product_id)

);
-- ddl-end --
COMMENT ON TABLE openchpl.product IS 'Table to store products that are submitted for vendors';
-- ddl-end --
ALTER TABLE openchpl.product OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.vendor | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.vendor CASCADE;
CREATE TABLE openchpl.vendor(
	vendor_id bigserial NOT NULL,
	address_id bigint,
	name varchar(300),
	website varchar(300),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT vendor_pk PRIMARY KEY (vendor_id)

);
-- ddl-end --
COMMENT ON TABLE openchpl.vendor IS 'Table to store vendors that are entered into the system';
-- ddl-end --
ALTER TABLE openchpl.vendor OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.user_permission | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.user_permission CASCADE;
CREATE TABLE openchpl.user_permission(
	user_permission_id bigserial NOT NULL,
	name varchar(25) NOT NULL,
	description varchar(1000) NOT NULL,
	authority varchar(255) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT user_permission_pk PRIMARY KEY (user_permission_id)

);
-- ddl-end --
ALTER TABLE openchpl.user_permission OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.certified_product | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.certified_product CASCADE;
CREATE TABLE openchpl.certified_product(
	certified_product_id bigserial NOT NULL,
	certification_edition_id bigint NOT NULL,
	product_version_id bigint NOT NULL,
	testing_lab_id bigint,
	certification_body_id bigint NOT NULL,
	chpl_product_number varchar(250),
	report_file_location varchar(255),
	quality_management_system_att text,
	atcb_certification_id varchar(250),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	practice_type_id bigint,
	product_classification_type_id bigint,
	CONSTRAINT certified_product_pk PRIMARY KEY (certified_product_id)

);
-- ddl-end --
COMMENT ON TABLE openchpl.certified_product IS 'A product that has been Certified';
-- ddl-end --
ALTER TABLE openchpl.certified_product OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.product_version | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.product_version CASCADE;
CREATE TABLE openchpl.product_version(
	product_version_id bigserial NOT NULL,
	product_id bigint NOT NULL,
	version varchar(250),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT product_version_pk PRIMARY KEY (product_version_id)

);
-- ddl-end --
COMMENT ON TABLE openchpl.product_version IS 'Table to store individual versions of a specific product';
-- ddl-end --
ALTER TABLE openchpl.product_version OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.certification_edition | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.certification_edition CASCADE;
CREATE TABLE openchpl.certification_edition(
	certification_edition_id bigserial NOT NULL,
	year varchar(10),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT certification_edition_pk PRIMARY KEY (certification_edition_id)

);
-- ddl-end --
ALTER TABLE openchpl.certification_edition OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.certification_criterion | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.certification_criterion CASCADE;
CREATE TABLE openchpl.certification_criterion(
	certification_criterion_id bigserial NOT NULL,
	certification_edition_id bigint NOT NULL,
	number varchar(15),
	title varchar(250),
	description varchar(1000),
	automated_numerator_capable bool,
	automated_measure_capable bool,
	requires_sed bool,
	parent_criterion_id bigint,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT certification_criterion_pk PRIMARY KEY (certification_criterion_id)

);
-- ddl-end --
ALTER TABLE openchpl.certification_criterion OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.certification_result | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.certification_result CASCADE;
CREATE TABLE openchpl.certification_result(
	certification_result_id bigserial NOT NULL,
	certification_criterion_id bigint NOT NULL,
	certified_product_id bigint NOT NULL,
	test_data_version_id bigint,
	test_procedure_version_id bigint,
	successful bool NOT NULL,
	inherited bool,
	gap bool,
	automated_numerator bool,
	automated_measure_capable bool,
	sed_successful bool,
	sed_inherited bool,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT certification_result_pk PRIMARY KEY (certification_result_id)

);
-- ddl-end --
ALTER TABLE openchpl.certification_result OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.testing_lab | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.testing_lab CASCADE;
CREATE TABLE openchpl.testing_lab(
	testing_lab_id bigserial NOT NULL,
	address_id bigint,
	name varchar(250) NOT NULL,
	accredidation_number varchar(25),
	website varchar(300),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT testing_lab_pk PRIMARY KEY (testing_lab_id)

);
-- ddl-end --
ALTER TABLE openchpl.testing_lab OWNER TO openchpl;
-- ddl-end --

-- object: vendor_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.product DROP CONSTRAINT IF EXISTS vendor_fk CASCADE;
ALTER TABLE openchpl.product ADD CONSTRAINT vendor_fk FOREIGN KEY (vendor_id)
REFERENCES openchpl.vendor (vendor_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: product_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.product_version DROP CONSTRAINT IF EXISTS product_fk CASCADE;
ALTER TABLE openchpl.product_version ADD CONSTRAINT product_fk FOREIGN KEY (product_id)
REFERENCES openchpl.product (product_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: product_version_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certified_product DROP CONSTRAINT IF EXISTS product_version_fk CASCADE;
ALTER TABLE openchpl.certified_product ADD CONSTRAINT product_version_fk FOREIGN KEY (product_version_id)
REFERENCES openchpl.product_version (product_version_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: testing_lab_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certified_product DROP CONSTRAINT IF EXISTS testing_lab_fk CASCADE;
ALTER TABLE openchpl.certified_product ADD CONSTRAINT testing_lab_fk FOREIGN KEY (testing_lab_id)
REFERENCES openchpl.testing_lab (testing_lab_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_body_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certified_product DROP CONSTRAINT IF EXISTS certification_body_fk CASCADE;
ALTER TABLE openchpl.certified_product ADD CONSTRAINT certification_body_fk FOREIGN KEY (certification_body_id)
REFERENCES openchpl.certification_body (certification_body_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_edition_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certified_product DROP CONSTRAINT IF EXISTS certification_edition_fk CASCADE;
ALTER TABLE openchpl.certified_product ADD CONSTRAINT certification_edition_fk FOREIGN KEY (certification_edition_id)
REFERENCES openchpl.certification_edition (certification_edition_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_criterion_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certification_result DROP CONSTRAINT IF EXISTS certification_criterion_fk CASCADE;
ALTER TABLE openchpl.certification_result ADD CONSTRAINT certification_criterion_fk FOREIGN KEY (certification_criterion_id)
REFERENCES openchpl.certification_criterion (certification_criterion_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.global_user_permission_map | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.global_user_permission_map CASCADE;
CREATE TABLE openchpl.global_user_permission_map(
	user_id bigint,
	user_permission_id_user_permission bigint,
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool DEFAULT false,
	CONSTRAINT global_user_permission_map_pk PRIMARY KEY (user_id,user_permission_id_user_permission)

);
-- ddl-end --

-- object: user_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.global_user_permission_map DROP CONSTRAINT IF EXISTS user_fk CASCADE;
ALTER TABLE openchpl.global_user_permission_map ADD CONSTRAINT user_fk FOREIGN KEY (user_id)
REFERENCES openchpl.user (user_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: user_permission_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.global_user_permission_map DROP CONSTRAINT IF EXISTS user_permission_fk CASCADE;
ALTER TABLE openchpl.global_user_permission_map ADD CONSTRAINT user_permission_fk FOREIGN KEY (user_permission_id_user_permission)
REFERENCES openchpl.user_permission (user_permission_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certified_product_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certification_result DROP CONSTRAINT IF EXISTS certified_product_fk CASCADE;
ALTER TABLE openchpl.certification_result ADD CONSTRAINT certified_product_fk FOREIGN KEY (certified_product_id)
REFERENCES openchpl.certified_product (certified_product_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.address | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.address CASCADE;
CREATE TABLE openchpl.address(
	address_id bigserial NOT NULL,
	street_line_1 varchar(250) NOT NULL,
	street_line_2 varchar(250),
	city varchar(250) NOT NULL,
	region varchar(250) NOT NULL,
	country varchar(250) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT now(),
	last_modified_date timestamp NOT NULL DEFAULT now(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT address_pk PRIMARY KEY (address_id)

);
-- ddl-end --
ALTER TABLE openchpl.address OWNER TO openchpl;
-- ddl-end --

-- object: address_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.vendor DROP CONSTRAINT IF EXISTS address_fk CASCADE;
ALTER TABLE openchpl.vendor ADD CONSTRAINT address_fk FOREIGN KEY (address_id)
REFERENCES openchpl.address (address_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: address_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.testing_lab DROP CONSTRAINT IF EXISTS address_fk CASCADE;
ALTER TABLE openchpl.testing_lab ADD CONSTRAINT address_fk FOREIGN KEY (address_id)
REFERENCES openchpl.address (address_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: address_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certification_body DROP CONSTRAINT IF EXISTS address_fk CASCADE;
ALTER TABLE openchpl.certification_body ADD CONSTRAINT address_fk FOREIGN KEY (address_id)
REFERENCES openchpl.address (address_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.contact | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.contact CASCADE;
CREATE TABLE openchpl.contact(
	contact_id bigserial NOT NULL,
	first_name varchar(250) NOT NULL,
	last_name varchar(250) NOT NULL,
	email varchar(250) NOT NULL,
	phone_number varchar(50) NOT NULL,
	title varchar(250),
	signature_date date,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT contact_pk PRIMARY KEY (contact_id)

);
-- ddl-end --
ALTER TABLE openchpl.contact OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.atl_contact_map | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.atl_contact_map CASCADE;
CREATE TABLE openchpl.atl_contact_map(
	contact_id bigint,
	testing_lab_id_testing_lab bigint,
	authorize_representative bool,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT atl_contact_map_pk PRIMARY KEY (contact_id,testing_lab_id_testing_lab)

);
-- ddl-end --

-- object: contact_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.atl_contact_map DROP CONSTRAINT IF EXISTS contact_fk CASCADE;
ALTER TABLE openchpl.atl_contact_map ADD CONSTRAINT contact_fk FOREIGN KEY (contact_id)
REFERENCES openchpl.contact (contact_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: testing_lab_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.atl_contact_map DROP CONSTRAINT IF EXISTS testing_lab_fk CASCADE;
ALTER TABLE openchpl.atl_contact_map ADD CONSTRAINT testing_lab_fk FOREIGN KEY (testing_lab_id_testing_lab)
REFERENCES openchpl.testing_lab (testing_lab_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.acb_contact_map | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.acb_contact_map CASCADE;
CREATE TABLE openchpl.acb_contact_map(
	contact_id bigint,
	certification_body_id_certification_body bigint,
	authorized_representative bool,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT acb_contact_map_pk PRIMARY KEY (contact_id,certification_body_id_certification_body)

);
-- ddl-end --

-- object: contact_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.acb_contact_map DROP CONSTRAINT IF EXISTS contact_fk CASCADE;
ALTER TABLE openchpl.acb_contact_map ADD CONSTRAINT contact_fk FOREIGN KEY (contact_id)
REFERENCES openchpl.contact (contact_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_body_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.acb_contact_map DROP CONSTRAINT IF EXISTS certification_body_fk CASCADE;
ALTER TABLE openchpl.acb_contact_map ADD CONSTRAINT certification_body_fk FOREIGN KEY (certification_body_id_certification_body)
REFERENCES openchpl.certification_body (certification_body_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.vendor_contact_map | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.vendor_contact_map CASCADE;
CREATE TABLE openchpl.vendor_contact_map(
	contact_id bigint,
	vendor_id_vendor bigint,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT vendor_contact_map_pk PRIMARY KEY (contact_id,vendor_id_vendor)

);
-- ddl-end --

-- object: contact_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.vendor_contact_map DROP CONSTRAINT IF EXISTS contact_fk CASCADE;
ALTER TABLE openchpl.vendor_contact_map ADD CONSTRAINT contact_fk FOREIGN KEY (contact_id)
REFERENCES openchpl.contact (contact_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: vendor_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.vendor_contact_map DROP CONSTRAINT IF EXISTS vendor_fk CASCADE;
ALTER TABLE openchpl.vendor_contact_map ADD CONSTRAINT vendor_fk FOREIGN KEY (vendor_id_vendor)
REFERENCES openchpl.vendor (vendor_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.cqm_edition | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.cqm_edition CASCADE;
CREATE TABLE openchpl.cqm_edition(
	cqm_edition_id bigserial NOT NULL,
	edition varchar(10),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT cqm_edition_pk PRIMARY KEY (cqm_edition_id)

);
-- ddl-end --
ALTER TABLE openchpl.cqm_edition OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.cqm_criterion | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.cqm_criterion CASCADE;
CREATE TABLE openchpl.cqm_criterion(
	cqm_criterion_id bigserial NOT NULL,
	cqm_edition_id bigint NOT NULL,
	cqm_criterion_type_id bigint NOT NULL,
	number varchar(20),
	cms_id varchar(15),
	title varchar(250),
	description varchar(1000),
	cqm_domain varchar(250),
	nqf_number varchar(50),
	cqm_version varchar(10),
	creation_date timestamp NOT NULL DEFAULT now(),
	last_modified_date timestamp NOT NULL DEFAULT now(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT cqm_criterion_pk PRIMARY KEY (cqm_criterion_id)

);
-- ddl-end --
ALTER TABLE openchpl.cqm_criterion OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.cqm_result | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.cqm_result CASCADE;
CREATE TABLE openchpl.cqm_result(
	cqm_result_id bigserial NOT NULL,
	cqm_version_id bigint,
	cqm_criterion_id bigint NOT NULL,
	success bool,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	certified_product_cqm_edition_map_id bigint NOT NULL,
	CONSTRAINT cqm_result_pk PRIMARY KEY (cqm_result_id)

);
-- ddl-end --
ALTER TABLE openchpl.cqm_result OWNER TO openchpl;
-- ddl-end --

-- object: certification_edition_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certification_criterion DROP CONSTRAINT IF EXISTS certification_edition_fk CASCADE;
ALTER TABLE openchpl.certification_criterion ADD CONSTRAINT certification_edition_fk FOREIGN KEY (certification_edition_id)
REFERENCES openchpl.certification_edition (certification_edition_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: cqm_edition_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.cqm_criterion DROP CONSTRAINT IF EXISTS cqm_edition_fk CASCADE;
ALTER TABLE openchpl.cqm_criterion ADD CONSTRAINT cqm_edition_fk FOREIGN KEY (cqm_edition_id)
REFERENCES openchpl.cqm_edition (cqm_edition_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: cqm_criterion_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.cqm_result DROP CONSTRAINT IF EXISTS cqm_criterion_fk CASCADE;
ALTER TABLE openchpl.cqm_result ADD CONSTRAINT cqm_criterion_fk FOREIGN KEY (cqm_criterion_id)
REFERENCES openchpl.cqm_criterion (cqm_criterion_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.cqm_criterion_type | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.cqm_criterion_type CASCADE;
CREATE TABLE openchpl.cqm_criterion_type(
	cqm_criterion_type_id bigserial NOT NULL,
	name varchar(50) NOT NULL,
	description varchar(250) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT cqm_criterion_type_pk PRIMARY KEY (cqm_criterion_type_id)

);
-- ddl-end --
ALTER TABLE openchpl.cqm_criterion_type OWNER TO openchpl;
-- ddl-end --

-- object: cqm_criterion_type_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.cqm_criterion DROP CONSTRAINT IF EXISTS cqm_criterion_type_fk CASCADE;
ALTER TABLE openchpl.cqm_criterion ADD CONSTRAINT cqm_criterion_type_fk FOREIGN KEY (cqm_criterion_type_id)
REFERENCES openchpl.cqm_criterion_type (cqm_criterion_type_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.criterion_standard | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.criterion_standard CASCADE;
CREATE TABLE openchpl.criterion_standard(
	criterion_standard_id bigserial NOT NULL,
	certification_criterion_id bigint NOT NULL,
	number varchar(25),
	description varchar(1000),
	sublist_description varchar(250),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL,
	CONSTRAINT criterion_standard_pk PRIMARY KEY (criterion_standard_id)

);
-- ddl-end --
ALTER TABLE openchpl.criterion_standard OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.criterion_optional_functionality | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.criterion_optional_functionality CASCADE;
CREATE TABLE openchpl.criterion_optional_functionality(
	criterion_optional_functionality_id bigserial NOT NULL,
	certification_criterion_id bigint NOT NULL,
	number varchar(25),
	description varchar(1000),
	creation_date timestamp NOT NULL DEFAULT now(),
	last_modified_date timestamp NOT NULL DEFAULT now(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT criterion_optional_functionality_pk PRIMARY KEY (criterion_optional_functionality_id)

);
-- ddl-end --
ALTER TABLE openchpl.criterion_optional_functionality OWNER TO openchpl;
-- ddl-end --

-- object: certification_criterion_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.criterion_standard DROP CONSTRAINT IF EXISTS certification_criterion_fk CASCADE;
ALTER TABLE openchpl.criterion_standard ADD CONSTRAINT certification_criterion_fk FOREIGN KEY (certification_criterion_id)
REFERENCES openchpl.certification_criterion (certification_criterion_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_criterion_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.criterion_optional_functionality DROP CONSTRAINT IF EXISTS certification_criterion_fk CASCADE;
ALTER TABLE openchpl.criterion_optional_functionality ADD CONSTRAINT certification_criterion_fk FOREIGN KEY (certification_criterion_id)
REFERENCES openchpl.certification_criterion (certification_criterion_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.certification_event | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.certification_event CASCADE;
CREATE TABLE openchpl.certification_event(
	certification_event_id bigserial NOT NULL,
	certified_product_id bigint NOT NULL,
	event_type_id bigint NOT NULL,
	event_date date NOT NULL,
	city varchar(250),
	state varchar(25),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT certification_events_pk PRIMARY KEY (certification_event_id)

);
-- ddl-end --
ALTER TABLE openchpl.certification_event OWNER TO openchpl;
-- ddl-end --

-- object: certified_product_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certification_event DROP CONSTRAINT IF EXISTS certified_product_fk CASCADE;
ALTER TABLE openchpl.certification_event ADD CONSTRAINT certified_product_fk FOREIGN KEY (certified_product_id)
REFERENCES openchpl.certified_product (certified_product_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.event_type | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.event_type CASCADE;
CREATE TABLE openchpl.event_type(
	event_type_id bigserial NOT NULL,
	name varchar(50) NOT NULL,
	description varchar(250) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT event_type_pk PRIMARY KEY (event_type_id)

);
-- ddl-end --
ALTER TABLE openchpl.event_type OWNER TO openchpl;
-- ddl-end --

-- object: event_type_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certification_event DROP CONSTRAINT IF EXISTS event_type_fk CASCADE;
ALTER TABLE openchpl.certification_event ADD CONSTRAINT event_type_fk FOREIGN KEY (event_type_id)
REFERENCES openchpl.event_type (event_type_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.product_classification_type | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.product_classification_type CASCADE;
CREATE TABLE openchpl.product_classification_type(
	product_classification_type_id bigserial NOT NULL,
	name varchar(50) NOT NULL,
	description varchar(250) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT product_classification_type_pk PRIMARY KEY (product_classification_type_id)

);
-- ddl-end --
ALTER TABLE openchpl.product_classification_type OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.practice_type | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.practice_type CASCADE;
CREATE TABLE openchpl.practice_type(
	practice_type_id bigserial NOT NULL,
	name varchar(50) NOT NULL,
	description varchar(250) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT practice_type_pk PRIMARY KEY (practice_type_id)

);
-- ddl-end --
ALTER TABLE openchpl.practice_type OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.additional_software | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.additional_software CASCADE;
CREATE TABLE openchpl.additional_software(
	additional_software_id bigserial NOT NULL,
	certified_product_id bigint NOT NULL,
	name varchar(500) NOT NULL,
	version varchar(250) NOT NULL,
	justification varchar(1000),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT additional_software_pk PRIMARY KEY (additional_software_id)

);
-- ddl-end --
ALTER TABLE openchpl.additional_software OWNER TO openchpl;
-- ddl-end --

-- object: certified_product_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.additional_software DROP CONSTRAINT IF EXISTS certified_product_fk CASCADE;
ALTER TABLE openchpl.additional_software ADD CONSTRAINT certified_product_fk FOREIGN KEY (certified_product_id)
REFERENCES openchpl.certified_product (certified_product_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.test_tool | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.test_tool CASCADE;
CREATE TABLE openchpl.test_tool(
	test_tool_id bigserial NOT NULL,
	certification_edition_id bigint NOT NULL,
	name varchar(100) NOT NULL,
	description varchar(1000),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT test_tool_pk PRIMARY KEY (test_tool_id)

);
-- ddl-end --
ALTER TABLE openchpl.test_tool OWNER TO openchpl;
-- ddl-end --

-- object: certification_edition_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_tool DROP CONSTRAINT IF EXISTS certification_edition_fk CASCADE;
ALTER TABLE openchpl.test_tool ADD CONSTRAINT certification_edition_fk FOREIGN KEY (certification_edition_id)
REFERENCES openchpl.certification_edition (certification_edition_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.test_tool_version | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.test_tool_version CASCADE;
CREATE TABLE openchpl.test_tool_version(
	test_tool_version_id bigserial NOT NULL,
	test_tool_id bigint NOT NULL,
	version varchar(50) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT test_tool_version_pk PRIMARY KEY (test_tool_version_id)

);
-- ddl-end --
ALTER TABLE openchpl.test_tool_version OWNER TO openchpl;
-- ddl-end --

-- object: test_tool_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_tool_version DROP CONSTRAINT IF EXISTS test_tool_fk CASCADE;
ALTER TABLE openchpl.test_tool_version ADD CONSTRAINT test_tool_fk FOREIGN KEY (test_tool_id)
REFERENCES openchpl.test_tool (test_tool_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.utilized_test_tool | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.utilized_test_tool CASCADE;
CREATE TABLE openchpl.utilized_test_tool(
	utilized_test_tool_id bigserial NOT NULL,
	selected bool NOT NULL,
	certified_product_id bigint NOT NULL,
	test_tool_id bigint,
	test_tool_version_id bigint,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT utilized_test_tool_pk PRIMARY KEY (utilized_test_tool_id)

);
-- ddl-end --
ALTER TABLE openchpl.utilized_test_tool OWNER TO openchpl;
-- ddl-end --

-- object: certified_product_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.utilized_test_tool DROP CONSTRAINT IF EXISTS certified_product_fk CASCADE;
ALTER TABLE openchpl.utilized_test_tool ADD CONSTRAINT certified_product_fk FOREIGN KEY (certified_product_id)
REFERENCES openchpl.certified_product (certified_product_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: test_tool_version_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.utilized_test_tool DROP CONSTRAINT IF EXISTS test_tool_version_fk CASCADE;
ALTER TABLE openchpl.utilized_test_tool ADD CONSTRAINT test_tool_version_fk FOREIGN KEY (test_tool_version_id)
REFERENCES openchpl.test_tool_version (test_tool_version_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: test_tool_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.utilized_test_tool DROP CONSTRAINT IF EXISTS test_tool_fk CASCADE;
ALTER TABLE openchpl.utilized_test_tool ADD CONSTRAINT test_tool_fk FOREIGN KEY (test_tool_id)
REFERENCES openchpl.test_tool (test_tool_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.standards_met | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.standards_met CASCADE;
CREATE TABLE openchpl.standards_met(
	standards_met_id bigserial NOT NULL,
	standard_met bool NOT NULL,
	criterion_standard_id bigint NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	certification_result_id bigint NOT NULL,
	CONSTRAINT standards_met_pk PRIMARY KEY (standards_met_id)

);
-- ddl-end --
ALTER TABLE openchpl.standards_met OWNER TO openchpl;
-- ddl-end --

-- object: criterion_standard_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.standards_met DROP CONSTRAINT IF EXISTS criterion_standard_fk CASCADE;
ALTER TABLE openchpl.standards_met ADD CONSTRAINT criterion_standard_fk FOREIGN KEY (criterion_standard_id)
REFERENCES openchpl.criterion_standard (criterion_standard_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.newer_standards_met | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.newer_standards_met CASCADE;
CREATE TABLE openchpl.newer_standards_met(
	newer_standards_met_id bigserial NOT NULL,
	certified_product_id bigint NOT NULL,
	certification_criterion_id bigint NOT NULL,
	version varchar(100) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT newer_standards_met_pk PRIMARY KEY (newer_standards_met_id)

);
-- ddl-end --
ALTER TABLE openchpl.newer_standards_met OWNER TO openchpl;
-- ddl-end --

-- object: certified_product_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.newer_standards_met DROP CONSTRAINT IF EXISTS certified_product_fk CASCADE;
ALTER TABLE openchpl.newer_standards_met ADD CONSTRAINT certified_product_fk FOREIGN KEY (certified_product_id)
REFERENCES openchpl.certified_product (certified_product_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_criterion_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.newer_standards_met DROP CONSTRAINT IF EXISTS certification_criterion_fk CASCADE;
ALTER TABLE openchpl.newer_standards_met ADD CONSTRAINT certification_criterion_fk FOREIGN KEY (certification_criterion_id)
REFERENCES openchpl.certification_criterion (certification_criterion_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.optional_functionality_met | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.optional_functionality_met CASCADE;
CREATE TABLE openchpl.optional_functionality_met(
	optional_functionality_met_id bigserial NOT NULL,
	criterion_optional_functionality_id bigint NOT NULL,
	functionality_met bool NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	certification_result_id bigint NOT NULL,
	CONSTRAINT optional_functionality_met_pk PRIMARY KEY (optional_functionality_met_id)

);
-- ddl-end --
ALTER TABLE openchpl.optional_functionality_met OWNER TO openchpl;
-- ddl-end --

-- object: criterion_optional_functionality_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.optional_functionality_met DROP CONSTRAINT IF EXISTS criterion_optional_functionality_fk CASCADE;
ALTER TABLE openchpl.optional_functionality_met ADD CONSTRAINT criterion_optional_functionality_fk FOREIGN KEY (criterion_optional_functionality_id)
REFERENCES openchpl.criterion_optional_functionality (criterion_optional_functionality_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.test_procedure_version | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.test_procedure_version CASCADE;
CREATE TABLE openchpl.test_procedure_version(
	test_procedure_version_id bigserial NOT NULL,
	certification_criterion_id bigint NOT NULL,
	version varchar(25) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT test_procedure_version_pk PRIMARY KEY (test_procedure_version_id)

);
-- ddl-end --
ALTER TABLE openchpl.test_procedure_version OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.test_data_version | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.test_data_version CASCADE;
CREATE TABLE openchpl.test_data_version(
	test_data_version_id bigserial NOT NULL,
	certification_criterion_id bigint NOT NULL,
	version varchar(25) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT test_data_version_pk PRIMARY KEY (test_data_version_id)

);
-- ddl-end --
ALTER TABLE openchpl.test_data_version OWNER TO openchpl;
-- ddl-end --

-- object: certification_criterion_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_procedure_version DROP CONSTRAINT IF EXISTS certification_criterion_fk CASCADE;
ALTER TABLE openchpl.test_procedure_version ADD CONSTRAINT certification_criterion_fk FOREIGN KEY (certification_criterion_id)
REFERENCES openchpl.certification_criterion (certification_criterion_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_criterion_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_data_version DROP CONSTRAINT IF EXISTS certification_criterion_fk CASCADE;
ALTER TABLE openchpl.test_data_version ADD CONSTRAINT certification_criterion_fk FOREIGN KEY (certification_criterion_id)
REFERENCES openchpl.certification_criterion (certification_criterion_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: test_data_version_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certification_result DROP CONSTRAINT IF EXISTS test_data_version_fk CASCADE;
ALTER TABLE openchpl.certification_result ADD CONSTRAINT test_data_version_fk FOREIGN KEY (test_data_version_id)
REFERENCES openchpl.test_data_version (test_data_version_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: test_procedure_version_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certification_result DROP CONSTRAINT IF EXISTS test_procedure_version_fk CASCADE;
ALTER TABLE openchpl.certification_result ADD CONSTRAINT test_procedure_version_fk FOREIGN KEY (test_procedure_version_id)
REFERENCES openchpl.test_procedure_version (test_procedure_version_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.cqm_version | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.cqm_version CASCADE;
CREATE TABLE openchpl.cqm_version(
	cqm_version_id bigserial NOT NULL,
	cqm_criterion_id bigint NOT NULL,
	version varchar(25) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT cqm_version_pk PRIMARY KEY (cqm_version_id)

);
-- ddl-end --
ALTER TABLE openchpl.cqm_version OWNER TO openchpl;
-- ddl-end --

-- object: cqm_criterion_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.cqm_version DROP CONSTRAINT IF EXISTS cqm_criterion_fk CASCADE;
ALTER TABLE openchpl.cqm_version ADD CONSTRAINT cqm_criterion_fk FOREIGN KEY (cqm_criterion_id)
REFERENCES openchpl.cqm_criterion (cqm_criterion_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: cqm_version_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.cqm_result DROP CONSTRAINT IF EXISTS cqm_version_fk CASCADE;
ALTER TABLE openchpl.cqm_result ADD CONSTRAINT cqm_version_fk FOREIGN KEY (cqm_version_id)
REFERENCES openchpl.cqm_version (cqm_version_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.test_data_alteration | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.test_data_alteration CASCADE;
CREATE TABLE openchpl.test_data_alteration(
	test_data_alteration_id bigserial NOT NULL,
	certified_product_id bigint NOT NULL,
	description varchar(1000) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT test_data_alteration_pk PRIMARY KEY (test_data_alteration_id)

);
-- ddl-end --
ALTER TABLE openchpl.test_data_alteration OWNER TO openchpl;
-- ddl-end --

-- object: certified_product_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_data_alteration DROP CONSTRAINT IF EXISTS certified_product_fk CASCADE;
ALTER TABLE openchpl.test_data_alteration ADD CONSTRAINT certified_product_fk FOREIGN KEY (certified_product_id)
REFERENCES openchpl.certified_product (certified_product_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.test_result_summary_version | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.test_result_summary_version CASCADE;
CREATE TABLE openchpl.test_result_summary_version(
	test_result_summary_version_id bigserial NOT NULL,
	certified_product_id bigint NOT NULL,
	version varchar(10) NOT NULL,
	description varchar(250) NOT NULL,
	change_date date NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT test_result_summary_version_pk PRIMARY KEY (test_result_summary_version_id)

);
-- ddl-end --
ALTER TABLE openchpl.test_result_summary_version OWNER TO openchpl;
-- ddl-end --

-- object: certified_product_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_result_summary_version DROP CONSTRAINT IF EXISTS certified_product_fk CASCADE;
ALTER TABLE openchpl.test_result_summary_version ADD CONSTRAINT certified_product_fk FOREIGN KEY (certified_product_id)
REFERENCES openchpl.certified_product (certified_product_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.test_event_details | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.test_event_details CASCADE;
CREATE TABLE openchpl.test_event_details(
	test_event_details_id bigserial NOT NULL,
	certification_event_id bigint NOT NULL,
	test_environment varchar(250),
	intended_user_description varchar(1000),
	user_task_description text,
	major_test_finding text,
	effectiveness text,
	efficiency text,
	satisfaction text,
	areas_for_improvement text,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT test_event_details_pk PRIMARY KEY (test_event_details_id)

);
-- ddl-end --
ALTER TABLE openchpl.test_event_details OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.test_participant | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.test_participant CASCADE;
CREATE TABLE openchpl.test_participant(
	test_paticipant_id bigserial NOT NULL,
	test_event_details_id bigint NOT NULL,
	gender char,
	age smallint,
	occupation varchar(50),
	assistive_technology_needs varchar(250),
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT test_participant_pk PRIMARY KEY (test_paticipant_id)

);
-- ddl-end --
ALTER TABLE openchpl.test_participant OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.education_type | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.education_type CASCADE;
CREATE TABLE openchpl.education_type(
	education_type_id bigint NOT NULL,
	name varchar(25) NOT NULL,
	description varchar(250) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT education_type_pk PRIMARY KEY (education_type_id)

);
-- ddl-end --
ALTER TABLE openchpl.education_type OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.experience_type | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.experience_type CASCADE;
CREATE TABLE openchpl.experience_type(
	experience_type_id bigint NOT NULL,
	name varchar(25) NOT NULL,
	description varchar(250) NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT experience_type_pk PRIMARY KEY (experience_type_id)

);
-- ddl-end --
ALTER TABLE openchpl.experience_type OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.test_task | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.test_task CASCADE;
CREATE TABLE openchpl.test_task(
	test_task_id bigserial NOT NULL,
	certification_criterion_id bigint,
	test_event_details_id bigint NOT NULL,
	name varchar(50) NOT NULL,
	description text,
	task_time_seconds bigint,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT test_task_pk PRIMARY KEY (test_task_id)

);
-- ddl-end --
ALTER TABLE openchpl.test_task OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.test_task_result | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.test_task_result CASCADE;
CREATE TABLE openchpl.test_task_result(
	test_task_result_id bigserial NOT NULL,
	test_task_id bigint NOT NULL,
	test_paticipant_id bigint NOT NULL,
	education_type_id bigint,
	computer_experience_id bigint,
	product_experience_id bigint,
	success bool,
	deviation bool,
	duration bigint,
	task_satisfication smallint,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT test_task_result_pk PRIMARY KEY (test_task_result_id)

);
-- ddl-end --
ALTER TABLE openchpl.test_task_result OWNER TO openchpl;
-- ddl-end --

-- object: test_event_details_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_task DROP CONSTRAINT IF EXISTS test_event_details_fk CASCADE;
ALTER TABLE openchpl.test_task ADD CONSTRAINT test_event_details_fk FOREIGN KEY (test_event_details_id)
REFERENCES openchpl.test_event_details (test_event_details_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_criterion_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_task DROP CONSTRAINT IF EXISTS certification_criterion_fk CASCADE;
ALTER TABLE openchpl.test_task ADD CONSTRAINT certification_criterion_fk FOREIGN KEY (certification_criterion_id)
REFERENCES openchpl.certification_criterion (certification_criterion_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: test_task_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_task_result DROP CONSTRAINT IF EXISTS test_task_fk CASCADE;
ALTER TABLE openchpl.test_task_result ADD CONSTRAINT test_task_fk FOREIGN KEY (test_task_id)
REFERENCES openchpl.test_task (test_task_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: test_event_details_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_participant DROP CONSTRAINT IF EXISTS test_event_details_fk CASCADE;
ALTER TABLE openchpl.test_participant ADD CONSTRAINT test_event_details_fk FOREIGN KEY (test_event_details_id)
REFERENCES openchpl.test_event_details (test_event_details_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: test_participant_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_task_result DROP CONSTRAINT IF EXISTS test_participant_fk CASCADE;
ALTER TABLE openchpl.test_task_result ADD CONSTRAINT test_participant_fk FOREIGN KEY (test_paticipant_id)
REFERENCES openchpl.test_participant (test_paticipant_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: education_type_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_task_result DROP CONSTRAINT IF EXISTS education_type_fk CASCADE;
ALTER TABLE openchpl.test_task_result ADD CONSTRAINT education_type_fk FOREIGN KEY (education_type_id)
REFERENCES openchpl.education_type (education_type_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: experience_type_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_task_result DROP CONSTRAINT IF EXISTS experience_type_fk CASCADE;
ALTER TABLE openchpl.test_task_result ADD CONSTRAINT experience_type_fk FOREIGN KEY (computer_experience_id)
REFERENCES openchpl.experience_type (experience_type_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_event_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_event_details DROP CONSTRAINT IF EXISTS certification_event_fk CASCADE;
ALTER TABLE openchpl.test_event_details ADD CONSTRAINT certification_event_fk FOREIGN KEY (certification_event_id)
REFERENCES openchpl.certification_event (certification_event_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.certified_product_checksum | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.certified_product_checksum CASCADE;
CREATE TABLE openchpl.certified_product_checksum(
	certified_product_checksum varchar(64),
	stg_certification_certification_id varchar(64) NOT NULL,
	CONSTRAINT certified_product_checksum_pkey PRIMARY KEY (stg_certification_certification_id)

);
-- ddl-end --
ALTER TABLE openchpl.certified_product_checksum OWNER TO openchpl;
-- ddl-end --

-- object: practice_type_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certified_product DROP CONSTRAINT IF EXISTS practice_type_fk CASCADE;
ALTER TABLE openchpl.certified_product ADD CONSTRAINT practice_type_fk FOREIGN KEY (practice_type_id)
REFERENCES openchpl.practice_type (practice_type_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: product_classification_type_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certified_product DROP CONSTRAINT IF EXISTS product_classification_type_fk CASCADE;
ALTER TABLE openchpl.certified_product ADD CONSTRAINT product_classification_type_fk FOREIGN KEY (product_classification_type_id)
REFERENCES openchpl.product_classification_type (product_classification_type_id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_result_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.optional_functionality_met DROP CONSTRAINT IF EXISTS certification_result_fk CASCADE;
ALTER TABLE openchpl.optional_functionality_met ADD CONSTRAINT certification_result_fk FOREIGN KEY (certification_result_id)
REFERENCES openchpl.certification_result (certification_result_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certification_result_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.standards_met DROP CONSTRAINT IF EXISTS certification_result_fk CASCADE;
ALTER TABLE openchpl.standards_met ADD CONSTRAINT certification_result_fk FOREIGN KEY (certification_result_id)
REFERENCES openchpl.certification_result (certification_result_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.certified_product_cqm_edition_map | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.certified_product_cqm_edition_map CASCADE;
CREATE TABLE openchpl.certified_product_cqm_edition_map(
	certified_product_cqm_edition_map_id bigserial NOT NULL,
	cqm_edition_id bigint NOT NULL,
	certified_product_id bigint NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL DEFAULT false,
	CONSTRAINT certified_product_cqm_edition_map_pk PRIMARY KEY (certified_product_cqm_edition_map_id)

);
-- ddl-end --
ALTER TABLE openchpl.certified_product_cqm_edition_map OWNER TO openchpl;
-- ddl-end --

-- object: cqm_edition_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certified_product_cqm_edition_map DROP CONSTRAINT IF EXISTS cqm_edition_fk CASCADE;
ALTER TABLE openchpl.certified_product_cqm_edition_map ADD CONSTRAINT cqm_edition_fk FOREIGN KEY (cqm_edition_id)
REFERENCES openchpl.cqm_edition (cqm_edition_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certified_product_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certified_product_cqm_edition_map DROP CONSTRAINT IF EXISTS certified_product_fk CASCADE;
ALTER TABLE openchpl.certified_product_cqm_edition_map ADD CONSTRAINT certified_product_fk FOREIGN KEY (certified_product_id)
REFERENCES openchpl.certified_product (certified_product_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: certified_product_cqm_edition_map_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.cqm_result DROP CONSTRAINT IF EXISTS certified_product_cqm_edition_map_fk CASCADE;
ALTER TABLE openchpl.cqm_result ADD CONSTRAINT certified_product_cqm_edition_map_fk FOREIGN KEY (certified_product_cqm_edition_map_id)
REFERENCES openchpl.certified_product_cqm_edition_map (certified_product_cqm_edition_map_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: openchpl.user_contact_map | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.user_contact_map CASCADE;
CREATE TABLE openchpl.user_contact_map(
	user_contact_map_id bigserial NOT NULL,
	user_id bigint NOT NULL,
	contact_id bigint NOT NULL,
	creation_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_date timestamp NOT NULL DEFAULT NOW(),
	last_modified_user bigint NOT NULL,
	deleted bool NOT NULL,
	CONSTRAINT user_contact_map_pk PRIMARY KEY (user_contact_map_id)

);
-- ddl-end --
ALTER TABLE openchpl.user_contact_map OWNER TO openchpl;
-- ddl-end --

-- object: user_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.user_contact_map DROP CONSTRAINT IF EXISTS user_fk CASCADE;
ALTER TABLE openchpl.user_contact_map ADD CONSTRAINT user_fk FOREIGN KEY (user_id)
REFERENCES openchpl.user (user_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: user_contact_map_uq | type: CONSTRAINT --
-- ALTER TABLE openchpl.user_contact_map DROP CONSTRAINT IF EXISTS user_contact_map_uq CASCADE;
ALTER TABLE openchpl.user_contact_map ADD CONSTRAINT user_contact_map_uq UNIQUE (user_id);
-- ddl-end --

-- object: contact_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.user_contact_map DROP CONSTRAINT IF EXISTS contact_fk CASCADE;
ALTER TABLE openchpl.user_contact_map ADD CONSTRAINT contact_fk FOREIGN KEY (contact_id)
REFERENCES openchpl.contact (contact_id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: user_contact_map_uq1 | type: CONSTRAINT --
-- ALTER TABLE openchpl.user_contact_map DROP CONSTRAINT IF EXISTS user_contact_map_uq1 CASCADE;
ALTER TABLE openchpl.user_contact_map ADD CONSTRAINT user_contact_map_uq1 UNIQUE (contact_id);
-- ddl-end --

-- object: openchpl.acl_class | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.acl_class CASCADE;
CREATE TABLE openchpl.acl_class(
	id bigserial NOT NULL,
	class character varying(100) NOT NULL,
	CONSTRAINT acl_class_uk UNIQUE (class),
	CONSTRAINT ack_class_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE openchpl.acl_class OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.acl_entry | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.acl_entry CASCADE;
CREATE TABLE openchpl.acl_entry(
	id bigserial NOT NULL,
	acl_object_identity bigint NOT NULL,
	ace_order integer NOT NULL,
	sid bigint NOT NULL,
	mask integer NOT NULL,
	granting bool NOT NULL,
	audit_success bool NOT NULL,
	audit_failure bigint NOT NULL,
	CONSTRAINT acl_entry_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE openchpl.acl_entry OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.acl_sid | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.acl_sid CASCADE;
CREATE TABLE openchpl.acl_sid(
	id bigserial NOT NULL,
	principal bool NOT NULL,
	sid character varying(100) NOT NULL,
	CONSTRAINT acl_sid_pk PRIMARY KEY (id),
	CONSTRAINT acl_sid_uk UNIQUE (principal,sid)

);
-- ddl-end --
ALTER TABLE openchpl.acl_sid OWNER TO openchpl;
-- ddl-end --

-- object: openchpl.acl_object_identity | type: TABLE --
-- DROP TABLE IF EXISTS openchpl.acl_object_identity CASCADE;
CREATE TABLE openchpl.acl_object_identity(
	id bigserial NOT NULL,
	object_id_class bigint NOT NULL,
	object_id_identity bigint NOT NULL,
	parent_object bigint,
	owner_sid bigint,
	entries_inheriting bool NOT NULL,
	CONSTRAINT acl_object_identity_pk PRIMARY KEY (id)

);
-- ddl-end --
ALTER TABLE openchpl.acl_object_identity OWNER TO openchpl;
-- ddl-end --

-- object: acl_class_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.acl_object_identity DROP CONSTRAINT IF EXISTS acl_class_fk CASCADE;
ALTER TABLE openchpl.acl_object_identity ADD CONSTRAINT acl_class_fk FOREIGN KEY (object_id_class)
REFERENCES openchpl.acl_class (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: acl_sid_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.acl_object_identity DROP CONSTRAINT IF EXISTS acl_sid_fk CASCADE;
ALTER TABLE openchpl.acl_object_identity ADD CONSTRAINT acl_sid_fk FOREIGN KEY (owner_sid)
REFERENCES openchpl.acl_sid (id) MATCH FULL
ON DELETE SET NULL ON UPDATE CASCADE;
-- ddl-end --

-- object: acl_object_identity_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.acl_entry DROP CONSTRAINT IF EXISTS acl_object_identity_fk CASCADE;
ALTER TABLE openchpl.acl_entry ADD CONSTRAINT acl_object_identity_fk FOREIGN KEY (acl_object_identity)
REFERENCES openchpl.acl_object_identity (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: acl_sid_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.acl_entry DROP CONSTRAINT IF EXISTS acl_sid_fk CASCADE;
ALTER TABLE openchpl.acl_entry ADD CONSTRAINT acl_sid_fk FOREIGN KEY (sid)
REFERENCES openchpl.acl_sid (id) MATCH FULL
ON DELETE RESTRICT ON UPDATE CASCADE;
-- ddl-end --

-- object: acl_entry_uk | type: CONSTRAINT --
-- ALTER TABLE openchpl.acl_entry DROP CONSTRAINT IF EXISTS acl_entry_uk CASCADE;
ALTER TABLE openchpl.acl_entry ADD CONSTRAINT acl_entry_uk UNIQUE (acl_object_identity,ace_order);
-- ddl-end --

-- object: parent_criterion_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.certification_criterion DROP CONSTRAINT IF EXISTS parent_criterion_fk CASCADE;
ALTER TABLE openchpl.certification_criterion ADD CONSTRAINT parent_criterion_fk FOREIGN KEY (parent_criterion_id)
REFERENCES openchpl.certification_criterion (certification_criterion_id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: product_experience_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.test_task_result DROP CONSTRAINT IF EXISTS product_experience_fk CASCADE;
ALTER TABLE openchpl.test_task_result ADD CONSTRAINT product_experience_fk FOREIGN KEY (product_experience_id)
REFERENCES openchpl.experience_type (experience_type_id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --

-- object: parent_object_fk | type: CONSTRAINT --
-- ALTER TABLE openchpl.acl_object_identity DROP CONSTRAINT IF EXISTS parent_object_fk CASCADE;
ALTER TABLE openchpl.acl_object_identity ADD CONSTRAINT parent_object_fk FOREIGN KEY (parent_object)
REFERENCES openchpl.acl_object_identity (id) MATCH FULL
ON DELETE NO ACTION ON UPDATE NO ACTION;
-- ddl-end --


