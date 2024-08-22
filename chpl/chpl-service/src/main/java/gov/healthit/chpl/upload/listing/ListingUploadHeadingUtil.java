package gov.healthit.chpl.upload.listing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.service.CertificationCriterionService;

@Component
public class ListingUploadHeadingUtil {

    private CertificationCriterionService criteriaService;

    @Autowired
    public ListingUploadHeadingUtil(CertificationCriterionService criteriaService) {
        this.criteriaService = criteriaService;
    }

    public static List<Heading> getRequiredHeadings() {
        return Arrays.asList(Heading.UNIQUE_ID, Heading.DEVELOPER, Heading.PRODUCT, Heading.VERSION);
    }

    public static Heading getHeading(String colName) {
        if (StringUtils.isEmpty(colName)) {
            return null;
        }
        for (Heading heading : Heading.values()) {
            if (heading.colNames.stream()
                    .anyMatch(headingColName -> headingColName.equalsIgnoreCase(colName.trim()))) {
                return heading;
            }
        }
        return null;
    }

    public List<String> getHeadingOptions(String heading) {
        if (getHeading(heading) != null) {
            return getHeading(heading).colNames;
        } else if (criteriaService.getAllowedCriterionHeadingsForNewListing().contains(heading)) {
            //TODO  This really should return the list of other headings that could be used for a criterion,
            //but I don't have a good way of mapping from one heading to all the other headings that could be used.
            //The result is if someone used a column 170_315_A_1__C and 170_315_A_1_C (one heading has one underscore
            //and the other has two underscores) those will not be detected as duplicates at this time.
            return Stream.of(heading).toList();
        }
        return List.of();
    }

    public boolean isValidHeading(String heading) {
        return getHeading(heading) != null
                || criteriaService.getAllowedCriterionHeadingsForNewListing().contains(heading);
    }

    public boolean isCriterionHeading(String headingVal) {
        if (criteriaService.getAllowedCriterionHeadingsForNewListing().contains(headingVal)) {
            return true;
        }
        return false;
    }

    public enum Heading {
        //while each heading can have multiple values that it matches to,
        //the values have to be unique among all headings for this to work
        UNIQUE_ID("UNIQUE_CHPL_ID__C", "UNIQUE_CHPL_ID_C", "UNIQUE_CHPL_ID", "CHPL_PRODUCT_NUMBER"),
        RECORD_STATUS("RECORD_STATUS__C"), //included so we don't give errors of unrecognized col; should not used anywhere
        DEVELOPER("VENDOR__C", "VENDOR_C", "VENDOR", "DEVELOPER__C", "DEVELOPER_C", "DEVELOPER"),
        PRODUCT("PRODUCT__C", "PRODUCT_C", "PRODUCT"),
        VERSION("VERSION__C", "VERSION_C", "VERSION"),
        EDITION("CERT_YEAR__C", "CERT_YEAR_C", "CERT_YEAR", "EDITION__C", "EDITION_C", "EDITION"),
        ACB_CERTIFICATION_ID("ACB_CERTIFICATION_ID__C", "ACB_CERTIFICATION_ID_C", "ACB_CERTIFICATION_ID"),
        CERTIFICATION_BODY_NAME("CERTIFYING_ACB__C", "CERTIFYING_ACB_C", "CERTIFYING_ACB", "CERTIFICATION_BODY__C", "CERTIFICATION_BODY_C", "CERTIFICATION_BODY"),
        TESTING_LAB_NAME("TESTING_ATL__C", "TESTING_ATL_C", "TESTING_ATL"),
        CERTIFICATION_DATE("CERTIFICATION_DATE__C", "CERTIFICATION_DATE_C", "CERTIFICATION_DATE"),
        DEVELOPER_ADDRESS("VENDOR_STREET_ADDRESS__C", "VENDOR_STREET_ADDRESS_C", "VENDOR_STREET_ADDRESS", "DEVELOPER_STREET_ADDRESS__C", "DEVELOPER_STREET_ADDRESS_C", "DEVELOPER_STREET_ADDRESS"),
        DEVELOPER_ADDRESS_LINE_2("VENDOR_STREET_ADDRESS_2__C", "VENDOR_STREET_ADDRESS_2_C", "VENDOR_STREET_ADDRESS_2", "DEVELOPER_STREET_ADDRESS_2__C", "DEVELOPER_STREET_ADDRESS_2_C", "DEVELOPER_STREET_ADDRESS_2"),
        DEVELOPER_STATE("VENDOR_STATE__C", "VENDOR_STATE_C", "VENDOR_STATE", "DEVELOPER_STATE__C", "DEVELOPER_STATE_C", "DEVELOPER_STATE"),
        DEVELOPER_CITY("VENDOR_CITY__C", "VENDOR_CITY_C", "VENDOR_CITY", "DEVELOPER_CITY__C", "DEVELOPER_CITY_C", "DEVELOPER_CITY"),
        DEVELOPER_ZIP("VENDOR_ZIP__C", "VENDOR_ZIP_C", "VENDOR_ZIP", "DEVELOPER_ZIP__C", "DEVELOPER_ZIP_C", "DEVELOPER_ZIP"),
        DEVELOPER_WEBSITE("VENDOR_WEBSITE__C", "VENDOR_WEBSITE_C", "VENDOR_WEBSITE", "DEVELOPER_WEBSITE__C", "DEVELOPER_WEBSITE_C", "DEVELOPER_WEBSITE"),
        SELF_DEVELOPER("SELF_DEVELOPER__C", "SELF_DEVELOPER_C", "SELF_DEVELOPER", "SELF-DEVELOPER"),
        DEVELOPER_EMAIL("VENDOR_EMAIL__C", "VENDOR_EMAIL_C", "VENDOR_EMAIL", "DEVELOPER_EMAIL__C", "DEVELOPER_EMAIL_C", "DEVELOPER_EMAIL"),
        DEVELOPER_PHONE("VENDOR_PHONE__C", "VENDOR_PHONE_C", "VENDOR_PHONE", "DEVELOPER_PHONE__C", "DEVELOPER_PHONE_C", "DEVELOPER_PHONE"),
        DEVELOPER_CONTACT_NAME("VENDOR_CONTACT_NAME__C", "VENDOR_CONTACT_NAME_C", "VENDOR_CONTACT_NAME", "DEVELOPER_CONTACT_NAME__C", "DEVELOPER_CONTACT_NAME_C", "DEVELOPER_CONTACT_NAME"),
        TARGETED_USERS("Developer-Identified Target Users", "Developer_Identified_Target_Users"),
        QMS_STANDARD_NAME("QMS Standard", "QMS_STANDARD"),
        QMS_STANDARD_APPLICABLE_CRITERIA("QMS Standard Applicable Criteria"),
        QMS_MODIFICATION("QMS Modification Description"),
        ICS("ICS"),
        ICS_SOURCE("ICS Source"),
        ACCESSIBILITY_CERTIFIED("Accessibility Certified"),
        ACCESSIBILITY_STANDARD("Accessibility Standard"),
        K_1_URL("170.523(k)(1) URL", "170.523 (k)(1) URL"),
        SVAP_NOTICE_URL("SVAP Notice URL"),
        RWT_PLANS_URL("RWT Plans URL", "RWT_Plans_URL"),
        RWT_PLANS_CHECK_DATE("RWT Plans Check Date"),
        RWT_RESULTS_URL("RWT Results URL", "RWT_Results_URL"),
        RWT_RESULTS_CHECK_DATE("RWT Results Check Date"),
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
        TASK_TIME_DEV_OBS("Task Time Deviation - Observed Seconds", "Task Time Deviation - Mean Observed Seconds", "Task Time Deviation Observed"),
        TASK_TIME_DEV_OPT("Task Time Deviation - Optimal Seconds", "Task Time Deviation - Mean Optimal Seconds", "Task Time Deviation Optimal"),
        TASK_ERRORS_MEAN("Task Errors  Mean(%)", "Task Errors Mean(%)", "Task Errors Mean"),
        TASK_ERRORS_STDDEV("Task Errors - Standard Deviation (%)", "Task Errors Standard Deviation"),
        TASK_RATING_SCALE("Task Rating - Scale Type", "Task Rating Scale Type"),
        TASK_RATING("Task Rating"),
        TASK_RATING_STDDEV("Task Rating - Standard Deviation", "Task Rating Standard Deviation"),
        MEASURE_DOMAIN("Measure Domain"),
        MEASURE_REQUIRED_TEST("Measure Required Test"),
        MEASURE_TYPE("Measure Type"),
        MEASURE_ASSOCIATED_CRITERIA("Measure Associated Criteria"),
        GAP("GAP"),
        PRIVACY_AND_SECURITY("Privacy and Security Framework"),
        FUNCTIONALITIES_TESTED("Functionality Tested"),
        OPTIONAL_STANDARD("Standard Tested Against", "Optional Standard"),
        HAS_ADDITIONAL_SOFTWARE("Additional Software"),
        ADDITIONAL_SOFTWARE_LISTING("CP Source"),
        ADDITIONAL_SOFTWARE_LISTING_GROUPING("CP Source Grouping"),
        ADDITONAL_SOFTWARE_NONLISTING("Non CP Source"),
        ADDITIONAL_SOFTWARE_NONLISTING_VERSION("Non CP Source Version"),
        ADDITIONAL_SOFTWARE_NONLISTING_GROUPING("Non CP Source Grouping"),
        TEST_DATA("Test Data"),
        TEST_DATA_VERSION("Test data version"),
        TEST_DATA_ALTERATION("Test data alteration"),
        TEST_DATA_ALTERATION_DESC("Test data alteration description"),
        CONFORMANCE_METHOD("Conformance Method"),
        CONFORMANCE_METHOD_VERSION("Conformance Method Version"),
        TEST_TOOL_NAME("Test tool name"),
        TEST_TOOL_VERSION("Test tool version"),
        EXPORT_DOCUMENTATION("Export Documentation"),
        ATTESTATION_ANSWER("Attestation Answer"),
        DOCUMENTATION_URL("Documentation URL"),
        USE_CASES("Use Cases"),
        SERVICE_BASE_URL_LIST("Service Base URL List"),
        API_DOCUMENTATION_LINK("API Documentation Link"),
        UCD_PROCESS("UCD Process Selected"),
        UCD_PROCESS_DETAILS("UCD Process Details"),
        SVAP_REG_TEXT("SVAP Reg Text Citation", "Regulatory Text Citation", "SVAP_REG_TEXT"),
        RISK_MANAGEMENT_SUMMARY_INFORMATION("Risk Management Summary Information"),
        STANDARD("Standard", "Standards", "standard", "standards"),
        CODE_SET("Code Set", "Code Sets", "Codeset", "CodeSet", "Codesets", "CodeSets", "codeset", "codesets");

        private List<String> colNames;

        Heading(String... csvColNames) {
            colNames = new ArrayList<String>();
            for (String csvColName : csvColNames) {
                colNames.add(csvColName);
            }
        }

        public String getHeading() {
            return this.colNames.get(0);
        }

        public String getNamesAsString() {
            String names = "";
            for (int i = 0; i < this.colNames.size(); i++) {
                if (i == this.colNames.size() - 1) {
                    names += " or ";
                }
                names += this.colNames.get(i);
                if (i < this.colNames.size() - 1) {
                    names += ", ";
                }
            }
            return names;
        }
    }

    public enum LegacyHeading {
        MACRA_MEASURE_G1("Measure Successfully Tested for G1"),
        MACRA_MEASURE_G2("Measure Successfully Tested for G2"),
        //This was always called "Standards" which is now used for "Standards"
        //but "Test Standards" are something different, so I'm not sure if we need this but maybe.
        TEST_STANDARD("Test Standards"),
        TEST_PROCEDURE("Test procedure"),
        TEST_PROCEDURE_VERSION("Test procedure version");

        private List<String> colNames;

        LegacyHeading(String... csvColNames) {
            colNames = new ArrayList<String>();
            for (String csvColName : csvColNames) {
                colNames.add(csvColName);
            }
        }

        public String getHeading() {
            return this.colNames.get(0);
        }
    }
}
