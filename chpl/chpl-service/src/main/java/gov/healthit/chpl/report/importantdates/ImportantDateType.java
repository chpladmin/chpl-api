package gov.healthit.chpl.report.importantdates;

public enum ImportantDateType {
    CRITERIA_EXPIRING("%s expires"),
    CRITERIA_AVAILABLE("%s becomes available"),
    ATTRIBUTE_AVAILABLE("%s %s becomes available"),
    ATTRIBUTE_EXPIRING("%s %s expires"),
    ATTRIBUTE_REQUIRED("%s %s is required"),
    ATTRIBUTE_EXTENSION_ENDS("Extension ends for %s %s"),
    CMS_ID_CREATION("CMS ID creation for year %s begins"),
    CMS_ID_OVERLAP_ENDS("CMS ID creation for year %s ends"),
    ATTESTATION_SUBMISSIONS_OPEN("Attestation submissions open"),
    ATTESTATION_SUBMISSIONS_CLOSE("Attestation submissions close"),
    RWT_RESULTS_SUBMISSION_BEGIN("RWT Results submissions begin"),
    RWT_RESULTS_SUBMISSION_END("RWT Results submissions end"),
    QUARTER_END("Quarter %s ends");

    private String unformattedDisplay;

    ImportantDateType(String unformattedDisplay) {
        this.unformattedDisplay = unformattedDisplay;
    }
    public String getUnformattedDisplay() {
        return this.unformattedDisplay;
    }
}
