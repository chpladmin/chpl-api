package gov.healthit.chpl;

/**
 * All FF4J feature flags.
 * @author alarned
 *
 */
public class FeatureList {
    /** Feature related to submitted Complaints. */
    public static final String COMPLAINTS = "complaints";
    /** System for Developers to submit change requests. */
    public static final String CHANGE_REQUEST = "change-request";
    /** Features that will be turned on as of the effective rule date. */
    public static final String EFFECTIVE_RULE_DATE = "effective-rule-date";
    /** Ability to have Developers as User. */
    public static final String ROLE_DEVELOPER = "role-developer";
    /** Function for ONC-ACBs to generate their required quarterly and annual surveillance reporting. */
    public static final String SURVEILLANCE_REPORTING = "surveillance-reporting";
}
