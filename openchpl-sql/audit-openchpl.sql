CREATE schema audit;
REVOKE CREATE ON schema audit FROM public;
ALTER schema audit OWNER TO openchpl;

CREATE TABLE audit.logged_actions (
    schema_name text NOT NULL,
    table_name text NOT NULL,
    user_name text,
    action_tstamp TIMESTAMP WITH TIME zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    action text NOT NULL CHECK (action IN ('I','D','U')),
    original_data text,
    new_data text,
    query text
) WITH (fillfactor=100);

ALTER TABLE audit.logged_actions OWNER TO openchpl;

REVOKE ALL ON audit.logged_actions FROM public;
GRANT SELECT ON audit.logged_actions TO public;

CREATE INDEX logged_actions_schema_table_idx
ON audit.logged_actions(((schema_name||'.'||table_name)::TEXT));

CREATE INDEX logged_actions_action_tstamp_idx
ON audit.logged_actions(action_tstamp);

CREATE INDEX logged_actions_action_idx
ON audit.logged_actions(action);

--
-- Now, define the actual trigger function:
--
CREATE OR REPLACE FUNCTION audit.if_modified_func() RETURNS TRIGGER AS $body$
DECLARE
    v_old_data TEXT;
    v_new_data TEXT;
BEGIN
    /* This dance with casting the NEW and OLD values to a ROW is not necessary in pg 9.0+ */

    IF (TG_OP = 'UPDATE') THEN
        v_old_data := ROW(OLD.*);
        v_new_data := ROW(NEW.*);
        INSERT INTO audit.logged_actions (schema_name, table_name, user_name, action, original_data, new_data, query)
        VALUES (TG_TABLE_SCHEMA::TEXT, TG_TABLE_NAME::TEXT, session_user::TEXT, substring(TG_OP,1,1), v_old_data, v_new_data, current_query());
        RETURN NEW;
    ELSIF (TG_OP = 'DELETE') THEN
        v_old_data := ROW(OLD.*);
        INSERT INTO audit.logged_actions (schema_name, table_name, user_name, action, original_data, query)
        VALUES (TG_TABLE_SCHEMA::TEXT, TG_TABLE_NAME::TEXT, session_user::TEXT, substring(TG_OP,1,1), v_old_data, current_query());
        RETURN OLD;
    ELSIF (TG_OP = 'INSERT') THEN
        v_new_data := ROW(NEW.*);
        INSERT INTO audit.logged_actions (schema_name, table_name, user_name, action, new_data, query)
        VALUES (TG_TABLE_SCHEMA::TEXT, TG_TABLE_NAME::TEXT, session_user::TEXT, substring(TG_OP,1,1), v_new_data, current_query());
        RETURN NEW;
    ELSE
        RAISE WARNING '[AUDIT.IF_MODIFIED_FUNC] - Other action occurred: %, at %',TG_OP,now();
        RETURN NULL;
    END IF;

EXCEPTION
    WHEN data_exception THEN
        RAISE WARNING '[AUDIT.IF_MODIFIED_FUNC] - UDF ERROR [DATA EXCEPTION] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM;
        RETURN NULL;
    WHEN unique_violation THEN
        RAISE WARNING '[AUDIT.IF_MODIFIED_FUNC] - UDF ERROR [UNIQUE] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM;
        RETURN NULL;
    WHEN OTHERS THEN
        RAISE WARNING '[AUDIT.IF_MODIFIED_FUNC] - UDF ERROR [OTHER] - SQLSTATE: %, SQLERRM: %',SQLSTATE,SQLERRM;
        RETURN NULL;
END;
$body$
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = pg_catalog, audit;

ALTER FUNCTION audit.if_modified_func() OWNER TO openchpl;

-- To add trigger to a table, use:
-- CREATE TRIGGER tablename_audit
-- AFTER INSERT OR UPDATE OR DELETE on tablename
-- FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();

CREATE TRIGGER user_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.user FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER certification_body_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.certification_body FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER product_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.product FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER vendor_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.vendor FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER user_permission_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.user_permission FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER certified_product_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.certified_product FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER product_version_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.product_version FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER certification_edition_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.certification_edition FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER certification_criterion_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.certification_criterion FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER certification_result_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.certification_result FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER testing_lab_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.testing_lab FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER global_user_permission_map_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.global_user_permission_map FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER address_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.address FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER contact_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.contact FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER atl_contact_map_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.atl_contact_map FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER acb_contact_map_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.acb_contact_map FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER vendor_contact_map_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.vendor_contact_map FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER cqm_criterion_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.cqm_criterion FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER cqm_result_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.cqm_result FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER cqm_criterion_type_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.cqm_criterion_type FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER criterion_standard_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.criterion_standard FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER criterion_optional_functionality_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.criterion_optional_functionality FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER certification_event_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.certification_event FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER event_type_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.event_type FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER product_classification_type_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.product_classification_type FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER practice_type_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.practice_type FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER additional_software_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.additional_software FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER test_tool_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.test_tool FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER test_tool_version_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.test_tool_version FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER utilized_test_tool_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.utilized_test_tool FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER standards_met_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.standards_met FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER newer_standards_met_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.newer_standards_met FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER optional_functionality_met_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.optional_functionality_met FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER test_procedure_version_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.test_procedure_version FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER test_data_version_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.test_data_version FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER cqm_version_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.cqm_version FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER test_data_alteration_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.test_data_alteration FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER test_result_summary_version_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.test_result_summary_version FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER test_event_details_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.test_event_details FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER test_participant_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.test_participant FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER education_type_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.education_type FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER experience_type_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.experience_type FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER test_task_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.test_task FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER test_task_result_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.test_task_result FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER certified_product_checksum_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.certified_product_checksum FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER user_contact_map_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.user_contact_map FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER acl_class_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.acl_class FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER acl_entry_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.acl_entry FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
CREATE TRIGGER acl_sid_audit AFTER INSERT OR UPDATE OR DELETE on openchpl.acl_sid FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();
