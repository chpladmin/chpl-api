package gov.healthit.chpl.scheduler.job.urlStatus;

public enum UrlType {
    ATL("ONC-ATL"),
    ACB("ONC-ACB"),
    DEVELOPER("Developer"),
    //transparency_attestation_url
    MANDATORY_DISCLOSURE_URL("Mandatory Disclosure URL"),
    //report_file_location
    TEST_RESULTS_SUMMARY("Test Results Summary"),
    //sed_report_file_location
    FULL_USABILITY_REPORT("Full Usability Report"),
    //api_documentation
    API_DOCUMENTATION("API Documentation"),
    // export_documentation
    EXPORT_DOCUMENTATION("Export Documentation"),
    // documentation_url
    DOCUMENTATION_URL("Documentation URL"),
    // use_cases
    USE_CASES("Use Cases"),
    //Real World Testing Plans
    REAL_WORLD_TESTING_PLANS("Real World Testing Plans"),
    //Real World Testing Results
    REAL_WORLD_TESTING_RESULTS("Real World Testing Results"),
    //SVAP Notice
    SVAP_NOTICE_URL("SVAP Notice URL");


    private String name;

    UrlType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static UrlType findByName(final String urlTypeName) {
        UrlType result = null;
        UrlType[] availableValues = values();
        for (int i = 0; i < availableValues.length && result == null; i++) {
            if (availableValues[i].getName().equalsIgnoreCase(urlTypeName)
                    || availableValues[i].name().equalsIgnoreCase(urlTypeName)) {
                result = availableValues[i];
            }
        }
        return result;
    }
}
