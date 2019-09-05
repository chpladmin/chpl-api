package gov.healthit.chpl.dto.scheduler;

public enum UrlType {
    ATL("ONC-ATL"),
    ACB("ONC-ACB"),
    DEVELOPER("Developer"),
    //transparency_attestation_url
    MANDATORY_DISCLOSURE_URL("Mandatory Disclosure URL"),
    //report_file_location
    TEST_RESULTS_SUMMARY("Test Results Summary"),
    //sed_report_file_location
    FULL_USABILITY_REPORT("Full Usability Report");

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
