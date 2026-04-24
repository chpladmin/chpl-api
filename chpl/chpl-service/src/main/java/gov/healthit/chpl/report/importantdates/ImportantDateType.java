package gov.healthit.chpl.report.importantdates;

public enum ImportantDateType {
    CRITERIA_EXPIRING("%s expires"),
    CRITERIA_REQUIRED("%s is required"),
    CRITERIA_EXTENSION_ENDS("Extension ends for %s"),
    CMS_ID_CREATION("CMS ID for year %s begin"),
    CMS_ID_OVERLAP_ENDS("CMS ID creation for year %s ends"),
    ATTESTATION_SUBMISSIONS_OPEN("Attestation submissions open"),
    ATTESTATIONS_SUBMISSIONS_CLOSE("Attestation submissions close"),
    ATTESTATION_APPROVAL_CLOSES("Attestation approvals must be complete"),
    RWT_PLANS_SUBMISSION_BEGIN("RWT Plans submissions begin for %s"),
    RWT_PLANS_SUBMISSION_END("RWT Plans submissions end for %s"),
    RWT_RESULTS_SUBMISSION_BEGIN("RWT Results submissions begin for %s"),
    RWT_RESULTS_SUBMISSION_END("RWT Results submissions end for %s"),
    QUARTER_END("Quarter %s ends");

    private String unformattedDisplay;

    ImportantDateType(String unformattedDisplay) {
        this.unformattedDisplay = unformattedDisplay;
    }
    public String getUnformattedDisplay() {
        return this.unformattedDisplay;
    }
}
