package gov.healthit.chpl.domain;

/**
 * The various things that can be found wrong with user-entered surveillance for listings.
 * @author kekey
 *
 */
public enum SurveillanceOversightRule {
    LONG_SUSPENSION("Lengthy Suspension Rule",
            "Products that are in status \"Suspended by ACB\" for more than 30 days"),
    CAP_NOT_APPROVED("CAP Not Approved Rule",
            "Greater than 75 days between Date of Determination and today() but NO CAP Approval Date"),
    CAP_NOT_STARTED("CAP Not Started Rule",
            "Greater than 10 days between Corrective Action Plan Approval Date and today() but NO Date Corrective Action Began"),
    CAP_NOT_COMPLETED("CAP Not Completed Rule",
            "Date Corrective Action Must Be Completed has passed with no Date Corrective Action Completed"),
    CAP_NOT_CLOSED("CAP Not Closed Rule",
            "Listing with an Open Non-Conformity where the certification status is \"Withdrawn by ACB/Developer/Developer Under Surveillance\""),
    NONCONFORMITY_OPEN_CAP_COMPLETE("Closed CAP with Open Nonconformity Rule",
            "Listing with an Open Non-Conformity where the CAP Was Completed Date is >= 45 days ago.");

    private String title;
    private String description;

    SurveillanceOversightRule(final String title, final String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
