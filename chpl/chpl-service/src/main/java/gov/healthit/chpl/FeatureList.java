package gov.healthit.chpl;

/**
 * All FF4J feature flags.
 * @author alarned
 *
 */
public class FeatureList {
    /** System for Developers to submit change requests. */
    public static final String CHANGE_REQUEST = "change-request";
    /** Features that will be turned on as of the effective rule date. */
    public static final String EFFECTIVE_RULE_DATE = "effective-rule-date";
    /** Features that will be turned on approximately one month after the effective rule date. */
    public static final String EFFECTIVE_RULE_DATE_PLUS_ONE_MONTH = "effective-rule-date-plus-one-month";
    /** Ability to have Developers as User. */
    public static final String ROLE_DEVELOPER = "role-developer";
}
