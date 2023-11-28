package gov.healthit.chpl.scheduler.job.urlStatus.data;

public enum UrlType {
    ATL("ONC-ATL"),
    ACB("ONC-ACB"),
    DEVELOPER("Developer"),
    MANDATORY_DISCLOSURE("Mandatory Disclosure"),
    TEST_RESULTS_SUMMARY("Test Results Summary"),
    FULL_USABILITY_REPORT("Full Usability Report"),
    API_DOCUMENTATION("API Documentation"),
    EXPORT_DOCUMENTATION("Export Documentation"),
    DOCUMENTATION("Documentation"),
    USE_CASES("Use Cases"),
    SERVICE_BASE_URL_LIST("Service Base URL List"),
    RISK_MANAGEMENT_SUMMARY_INFORMATION("Risk Management Summary Information"),
    REAL_WORLD_TESTING_PLANS("Real World Testing Plans"),
    REAL_WORLD_TESTING_RESULTS("Real World Testing Results"),
    STANDARDS_VERSION_ADVANCEMENT_PROCESS_NOTICE("Standards Version Advancement Process Notice");

    private String name;

    UrlType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static UrlType findByName(String urlTypeName) {
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
