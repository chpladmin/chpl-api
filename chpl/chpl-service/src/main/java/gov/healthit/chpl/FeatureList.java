package gov.healthit.chpl;

public final class FeatureList {
    private FeatureList() {
    }

    /** System for Developers to submit change requests. */
    public static final String CHANGE_REQUEST = "change-request";
    /** Features that will be turned on as of the effective rule date. */
    public static final String EFFECTIVE_RULE_DATE = "effective-rule-date";
    /** Features that will be turned on as of one week past the effective rule date. */
    public static final String EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK = "effective-rule-date-plus-one-week";
    /** Ability to have Developers as User. */
    public static final String ROLE_DEVELOPER = "role-developer";
    public static final String RULE_PUBLISH_DATE_PLUS_THIRTY_DAYS = "rule-publish-date-plus-thirty-days";
}
