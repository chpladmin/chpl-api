SET statement_timeout = 0;
SET lock_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET search_path = openchpl, pg_catalog;

INSERT INTO acl_class VALUES (1, 'gov.healthit.chpl.auth.dto.UserDTO'), (2, 'gov.healthit.chpl.dto.CertificationBodyDTO');
SELECT pg_catalog.setval('acl_class_id_seq', 2, true);

INSERT INTO acl_sid VALUES (-2, true, 'admin');

INSERT INTO acl_object_identity VALUES (-2, 1, -2, NULL, -2, true);
INSERT INTO acl_object_identity VALUES (1, 2, 1, NULL, -2, true);
INSERT INTO acl_object_identity VALUES (2, 2, 2, NULL, -2, true);
INSERT INTO acl_object_identity VALUES (3, 2, 3, NULL, -2, true);
INSERT INTO acl_object_identity VALUES (4, 2, 4, NULL, -2, true);
INSERT INTO acl_object_identity VALUES (5, 2, 5, NULL, -2, true);
INSERT INTO acl_object_identity VALUES (6, 2, 6, NULL, -2, true);
INSERT INTO acl_object_identity VALUES (7, 2, 7, NULL, -2, true);

INSERT INTO acl_entry VALUES (1, -2, 0, -2, 16, true, false, false);
INSERT INTO acl_entry VALUES (2, 1, 0, -2, 16, true, false, false);
INSERT INTO acl_entry VALUES (3, 2, 0, -2, 16, true, false, false);
INSERT INTO acl_entry VALUES (4, 3, 0, -2, 16, true, false, false);
INSERT INTO acl_entry VALUES (5, 4, 0, -2, 16, true, false, false);
INSERT INTO acl_entry VALUES (6, 5, 0, -2, 16, true, false, false);
INSERT INTO acl_entry VALUES (7, 6, 0, -2, 16, true, false, false);
INSERT INTO acl_entry VALUES (8, 7, 0, -2, 16, true, false, false);

SELECT pg_catalog.setval('acl_entry_id_seq', 12, true);
SELECT pg_catalog.setval('acl_object_identity_id_seq', 7, true);
SELECT pg_catalog.setval('acl_sid_id_seq', 1, true);
INSERT INTO contact (contact_id, first_name, last_name, email, phone_number, signature_date, last_modified_user) VALUES (-2, 'Administrator', 'Administrator', 'info@ainq.com', '(301) 560-6999', '2015-09-13', -1);

SELECT pg_catalog.setval('contact_contact_id_seq', 1, true);
INSERT INTO "user" (user_id, user_name, password, account_expired, account_locked, credentials_expired, account_enabled, last_modified_user, contact_id) VALUES (-2, 'admin', '$2a$10$vVXOupd9DckGsQPtZ5h9seYCGzqYb3A35r/GNuP/rRbK2eq2KxtA2', false, false, false, true, -1, -2);
INSERT INTO user_permission (user_permission_id, "name", description, authority, last_modified_user) VALUES (-2, 'ADMIN', 'This permission confers administrative privileges to its owner.', 'ROLE_ADMIN', -1);

SELECT pg_catalog.setval('user_permission_user_permission_id_seq', 1, true);
INSERT INTO user_permission ("name", description, authority, last_modified_user) VALUES
('USER_CREATOR' ,'This permission allows a user to create other users',	'ROLE_USER_CREATOR' , -1),
('ACB_ADMIN' ,'This permission gives a user write access to their ACBs.',	'ROLE_ACB_ADMIN' , -1),
('ACB_STAFF' ,'This permission gives a user read access to their ACBs',	'ROLE_ACB_STAFF' , -1);
INSERT INTO global_user_permission_map (user_id, user_permission_id_user_permission, last_modified_user) VALUES (-2, -2, -1);

SELECT pg_catalog.setval('global_user_permission_map_global_user_permission_id_seq', 1, true);

SELECT pg_catalog.setval('user_user_id_seq', 4, true);
