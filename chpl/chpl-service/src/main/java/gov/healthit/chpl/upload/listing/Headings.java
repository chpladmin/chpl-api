package gov.healthit.chpl.upload.listing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public enum Headings {
    //while each heading can have multiple values that it matches to,
    //the values have to be unique among all headings for this to work
    UNIQUE_ID("UNIQUE_CHPL_ID__C", "UNIQUE_CHPL_ID_C", "UNIQUE_CHPL_ID", "CHPL_PRODUCT_NUMBER"),
    RECORD_STATUS("RECORD_STATUS__C"), //included so we don't give errors of unrecognized col; should not used anywhere
    DEVELOPER("VENDOR__C", "VENDOR_C", "VENDOR", "DEVELOPER__C", "DEVELOPER_C", "DEVELOPER"),
    PRODUCT("PRODUCT__C", "PRODUCT_C", "PRODUCT"),
    VERSION("VERSION__C", "VERSION_C", "VERSION"),
    EDITION("CERT_YEAR__C", "CERT_YEAR_C", "CERT_YEAR", "EDITION__C", "EDITION_C", "EDITION"),
    ACB_CERTIFICATION_ID("ACB_CERTIFICATION_ID__C", "ACB_CERTIFICATION_ID_C", "ACB_CERTIFICATION_ID"),
    CERTIFICATION_BODY_NAME("CERTIFYING_ACB__C", "CERTIFYING_ACB_C", "CERTIFYING_ACB",
            "CERTIFICATION_BODY__C", "CERTIFICATION_BODY_C", "CERTIFICATION_BODY"),
    TESTING_LAB_NAME("TESTING_ATL__C", "TESTING_ATL_C", "TESTING_ATL"),
    CERTIFICATION_DATE("CERTIFICATION_DATE__C", "CERTIFICATION_DATE_C", "CERTIFICATION_DATE"),
    DEVELOPER_ADDRESS("VENDOR_STREET_ADDRESS__C", "VENDOR_STREET_ADDRESS_C", "VENDOR_STREET_ADDRESS",
            "DEVELOPER_STREET_ADDRESS__C", "DEVELOPER_STREET_ADDRESS_C", "DEVELOPER_STREET_ADDRESS"),
    DEVELOPER_STATE("VENDOR_STATE__C", "VENDOR_STATE_C", "VENDOR_STATE", "DEVELOPER_STATE__C",
            "DEVELOPER_STATE_C", "DEVELOPER_STATE"),
    DEVELOPER_CITY("VENDOR_CITY__C", "VENDOR_CITY_C", "VENDOR_CITY", "DEVELOPER_CITY__C", "DEVELOPER_CITY_C",
            "DEVELOPER_CITY"),
    DEVELOPER_ZIP("VENDOR_ZIP__C", "VENDOR_ZIP_C", "VENDOR_ZIP", "DEVELOPER_ZIP__C",
            "DEVELOPER_ZIP_C", "DEVELOPER_ZIP"),
    DEVELOPER_WEBSITE("VENDOR_WEBSITE__C", "VENDOR_WEBSITE_C", "VENDOR_WEBSITE", "DEVELOPER_WEBSITE__C",
            "DEVELOPER_WEBSITE_C", "DEVELOPER_WEBSITE"),
    SELF_DEVELOPER("SELF_DEVELOPER__C", "SELF_DEVELOPER_C", "SELF_DEVELOPER", "SELF-DEVELOPER"),
    DEVELOPER_EMAIL("VENDOR_EMAIL__C", "VENDOR_EMAIL_C", "VENDOR_EMAIL", "DEVELOPER_EMAIL__C",
            "DEVELOPER_EMAIL_C", "DEVELOPER_EMAIL"),
    DEVELOPER_PHONE("VENDOR_PHONE__C", "VENDOR_PHONE_C", "VENDOR_PHONE", "DEVELOPER_PHONE__C",
            "DEVELOPER_PHONE_C", "DEVELOPER_PHONE"),
    DEVELOPER_CONTACT_NAME("VENDOR_CONTACT_NAME__C", "VENDOR_CONTACT_NAME_C", "VENDOR_CONTACT_NAME",
            "DEVELOPER_CONTACT_NAME__C", "DEVELOPER_CONTACT_NAME_C", "DEVELOPER_CONTACT_NAME"),
    TARGETED_USERS("Developer-Identified Target Users", "Developer_Identified_Target_Users"),
    QMS_STANDARD_NAME("QMS Standard", "QMS_STANDARD"),
    QMS_STANDARD_APPLICABLE_CRITERIA("QMS Standard Applicable Criteria"),
    QMS_MODIFICATION("QMS Modification Description"),
    ICS("ICS"),
    ICS_SOURCE("ICS Source"),
    ACCESSIBILITY_CERTIFIED("Accessibility Certified"),
    ACCESSIBILITY_STANDARD("Accessibility Standard"),
    K_1_URL("170.523(k)(1) URL", "170.523 (k)(1) URL"),
    CQM_NUMBER("CQM Number", "CQM_NUMBER"),
    CQM_VERSION("CQM Version", "CQM_VERSION"),
    CQM_CRITERIA("CQM Criteria", "CQM_CRITERIA"),
    SED_REPORT_URL("SED Report Hyperlink", "SED Report URL", "SED_REPORT_URL"),
    SED_INTENDED_USERS("Description of the Intended Users"),
    SED_TESTING_DATE("Date SED Testing was Concluded"),
    PARTICIPANT_ID("Participant Identifier"),
    PARTICIPANT_GENDER("Participant Gender"),
    PARTICIPANT_AGE("Participant Age"),
    PARTICIPANT_EDUCATION("Participant Education"),
    PARTICIPANT_OCCUPATION("Participant Occupation/Role", "Participant Occupation", "Participant Role"),
    PARTICIPANT_PROFESSIONAL_EXPERIENCE("Participant Professional Experience"),
    PARTICIPANT_COMPUTER_EXPERIENCE("Participant Computer Experience"),
    PARTICIPANT_PRODUCT_EXPERIENCE("Participant Product Experience"),
    PARTICIPANT_ASSISTIVE_TECH("Participant Assistive Technology Needs"),
    TASK_ID("Task Identifier"),
    TASK_DESCRIPTION("Task Description"),
    TASK_SUCCESS_MEAN("Task Success - Mean (%)", "Task Success Mean"),
    TASK_SUCCESS_STDDEV("Task Success - Standard Deviation (%)", "Task Success Standard Deviation"),
    TASK_PATH_DEV_OBS("Task Path Deviation - Observed #", "Task Path Deviation Observed"),
    TASK_PATH_DEV_OPT("Task Path Deviation - Optimal #", "Task Path Deviation Optimal"),
    TASK_TIME_MEAN("Task Time - Mean (seconds)", "Task Time Mean"),
    TASK_TIME_STDDEV("Task Time - Standard Deviation (seconds)", "Task Time Standard Deviation"),
    TASK_TIME_DEV_OBS("Task Time Deviation - Observed Seconds", "Task Time Deviation Observed"),
    TASK_TIME_DEV_OPT("Task Time Deviation - Optimal Seconds", "Task Time Deviation Optimal"),
    TASK_ERRORS_MEAN("Task Errors  Mean(%)", "Task Errors Mean"),
    TASK_ERRORS_STDDEV("Task Errors - Standard Deviation (%)", "Task Errors Standard Deviation"),
    TASK_RATING_SCALE("Task Rating - Scale Type", "Task Rating Scale Type"),
    TASK_RATING("Task Rating"),
    TASK_RATING_STDDEV("Task Rating - Standard Deviation", "Task Rating Standard Deviation"),
    GAP(HeadingLevel.CERT_RESULT, "GAP"),
    PRIVACY_AND_SECURITY(HeadingLevel.CERT_RESULT, "Privacy and Security Framework"),
    TEST_FUNCTIONALITY(HeadingLevel.CERT_RESULT, "Functionality Tested"),
    TEST_STANDARD(HeadingLevel.CERT_RESULT, "Standard Tested Against"),
    MACRA_MEASURE_G1(HeadingLevel.CERT_RESULT, "Measure Successfully Tested for G1"),
    MACRA_MEASURE_G2(HeadingLevel.CERT_RESULT, "Measure Successfully Tested for G2"),
    ADDITIONAL_SOFTWARE(HeadingLevel.CERT_RESULT, "Additional Software"),
    ADDITIONAL_SOFTWARE_LISTING(HeadingLevel.CERT_RESULT, "CP Source"),
    ADDITIONAL_SOFTWARE_LISTING_GROUPING(HeadingLevel.CERT_RESULT, "CP Source Grouping"),
    ADDITONAL_SOFTWARE_NONLISTING(HeadingLevel.CERT_RESULT, "Non CP Source"),
    ADDITIONAL_SOFTWARE_NONLISTING_VERSION(HeadingLevel.CERT_RESULT, "Non CP Source Version"),
    ADDITIONAL_SOFTWARE_NONLISTING_GROUPING(HeadingLevel.CERT_RESULT, "Non CP Source Grouping"),
    TEST_DATA(HeadingLevel.CERT_RESULT, "Test Data"),
    TEST_DATA_VERSION(HeadingLevel.CERT_RESULT, "Test data version"),
    TEST_DATA_ALTERATION(HeadingLevel.CERT_RESULT, "Test data alteration"),
    TEST_DATA_ALTERATION_DESC(HeadingLevel.CERT_RESULT, "Test data alteration description"),
    TEST_PROCEDURE(HeadingLevel.CERT_RESULT, "Test procedure"),
    TEST_PROCEDURE_VERSION(HeadingLevel.CERT_RESULT, "Test procedure version"),
    TEST_TOOL_NAME(HeadingLevel.CERT_RESULT, "Test tool name"),
    TEST_TOOL_VERSION(HeadingLevel.CERT_RESULT, "Test tool version"),
    EXPORT_DOCUMENTATION(HeadingLevel.CERT_RESULT, "Export Documentation"),
    ATTESTATION_ANSWER(HeadingLevel.CERT_RESULT, "Attestation Answer"),
    DOCUMENTATION_URL(HeadingLevel.CERT_RESULT, "Documentation URL"),
    USE_CASES(HeadingLevel.CERT_RESULT, "Use Cases"),
    API_DOCUMENTATION_LINK(HeadingLevel.CERT_RESULT, "API Documentation Link"),
    UCD_PROCESS(HeadingLevel.CERT_RESULT, "UCD Process Selected"),
    UCD_PROCESS_DETAILS(HeadingLevel.CERT_RESULT, "UCD Process Details"),

    CRITERIA_170_315_A_1(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_1__C"),
    CRITERIA_170_315_A_2(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_2__C"),
    CRITERIA_170_315_A_3(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_3__C"),
    CRITERIA_170_315_A_4(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_4__C"),
    CRITERIA_170_315_A_5(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_5__C"),
    CRITERIA_170_315_A_9(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_9__C"),
    CRITERIA_170_315_A_10(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_10__C"),
    CRITERIA_170_315_A_12(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_12__C"),
    CRITERIA_170_315_A_13(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_13__C"),
    CRITERIA_170_315_A_14(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_14__C"),
    CRITERIA_170_315_A_15(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_A_15__C"),
    CRITERIA_170_315_B_1(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_1__C"),
    CRITERIA_170_315_B_1_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_1_Cures__C"),
    CRITERIA_170_315_B_2(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_2__C"),
    CRITERIA_170_315_B_2_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_2_Cures__C"),
    CRITERIA_170_315_B_3_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_3_Cures__C"),
    CRITERIA_170_315_B_6(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_6__C"),
    CRITERIA_170_315_B_7(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_7__C"),
    CRITERIA_170_315_B_7_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_7_Cures__C"),
    CRITERIA_170_315_B_8(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_8__C"),
    CRITERIA_170_315_B_8_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_8_Cures__C"),
    CRITERIA_170_315_B_9(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_9__C"),
    CRITERIA_170_315_B_9_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_9_Cures__C"),
    CRITERIA_170_315_B_10(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_B_10__C"),
    CRITERIA_170_315_C_1(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_C_1__C"),
    CRITERIA_170_315_C_2(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_C_2__C"),
    CRITERIA_170_315_C_3(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_C_3__C"),
    CRITERIA_170_315_C_3_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_C_3_Cures__C"),
    CRITERIA_170_315_C_4(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_C_4__C"),
    CRITERIA_170_315_D_1(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_1__C"),
    CRITERIA_170_315_D_2(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_2__C"),
    CRITERIA_170_315_D_2_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_2_Cures__C"),
    CRITERIA_170_315_D_3(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_3__C"),
    CRITERIA_170_315_D_3_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_3_Cures__C"),
    CRITERIA_170_315_D_4(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_4__C"),
    CRITERIA_170_315_D_5(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_5__C"),
    CRITERIA_170_315_D_6(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_6__C"),
    CRITERIA_170_315_D_7(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_7__C"),
    CRITERIA_170_315_D_8(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_8__C"),
    CRITERIA_170_315_D_9(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_9__C"),
    CRITERIA_170_315_D_10(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_10__C"),
    CRITERIA_170_315_D_10_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_10_Cures__C"),
    CRITERIA_170_315_D_11(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_11__C"),
    CRITERIA_170_315_D_12(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_12__C"),
    CRITERIA_170_315_D_13(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_D_13__C"),
    CRITERIA_170_315_E_1(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_E_1__C"),
    CRITERIA_170_315_E_1_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_E_1_Cures__C"),
    CRITERIA_170_315_E_2(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_E_2__C"),
    CRITERIA_170_315_E_3(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_E_3__C"),
    CRITERIA_170_315_F_1(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_F_1__C"),
    CRITERIA_170_315_F_2(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_F_2__C"),
    CRITERIA_170_315_F_3(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_F_3__C"),
    CRITERIA_170_315_F_4(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_F_4__C"),
    CRITERIA_170_315_F_5(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_F_5__C"),
    CRITERIA_170_315_F_5_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_F_5_Cures__C"),
    CRITERIA_170_315_F_6(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_F_6__C"),
    CRITERIA_170_315_F_7(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_F_7__C"),
    CRITERIA_170_315_G_1(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_1__C"),
    CRITERIA_170_315_G_2(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_2__C"),
    CRITERIA_170_315_G_3(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_3__C"),
    CRITERIA_170_315_G_4(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_4__C"),
    CRITERIA_170_315_G_5(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_5__C"),
    CRITERIA_170_315_G_6(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_6__C"),
    CRITERIA_170_315_G_6_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_6_Cures__C"),
    CRITERIA_170_315_G_7(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_7__C"),
    CRITERIA_170_315_G_8(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_8__C"),
    CRITERIA_170_315_G_9(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_9__C"),
    CRITERIA_170_315_G_9_CURES(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_9_CURES__C"),
    CRITERIA_170_315_G_10(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_G_10__C"),
    CRITERIA_170_315_H_1(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_H_1__C"),
    CRITERIA_170_315_H_2(HeadingLevel.CERT_RESULT, "CRITERIA_170_315_H_2__C");

    private HeadingLevel level;
    private List<String> colNames;

    Headings(String...csvColNames) {
        this.level = HeadingLevel.LISTING;
        colNames = new ArrayList<String>();
        for (String csvColName : csvColNames) {
            colNames.add(csvColName);
        }
    }

    Headings(HeadingLevel level, String...csvColNames) {
        this.level = level;
        colNames = new ArrayList<String>();
        for (String csvColName : csvColNames) {
            colNames.add(csvColName);
        }
    }

    public String getNamesAsString() {
        String names = "";
        for (int i = 0; i < this.colNames.size(); i++) {
            names += this.colNames.get(i);
            if (i < this.colNames.size() - 1) {
                names += ", ";
            }
            if (i == this.colNames.size() - 1) {
                names += " or ";
            }
        }
        return names;
    }

    public HeadingLevel getLevel() {
        return this.level;
    }

    public static Headings getHeading(String colName) {
        if (StringUtils.isEmpty(colName)) {
            return null;
        }
        for (Headings heading : values()) {
            if (heading.colNames.stream()
                    .anyMatch(headingColName -> headingColName.equalsIgnoreCase(colName.trim()))) {
                return heading;
            }
        }
        return null;
    }

    public static List<Headings> getRequiredHeadings() {
        return Arrays.asList(Headings.UNIQUE_ID, Headings.DEVELOPER, Headings.PRODUCT, Headings.VERSION);
    }
}
