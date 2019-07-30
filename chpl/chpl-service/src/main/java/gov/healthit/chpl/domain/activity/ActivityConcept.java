package gov.healthit.chpl.domain.activity;

import java.io.Serializable;

/**
 * Different types of activity the system will log.
 *
 * @author kekey
 *
 */
public enum ActivityConcept implements Serializable {
    CERTIFIED_PRODUCT,
    PRODUCT,
    DEVELOPER,
    CERTIFICATION,
    CQM,
    CERTIFICATION_BODY,
    VERSION,
    USER,
    TESTING_LAB,
    PENDING_CERTIFIED_PRODUCT,
    API_KEY,
    ANNOUNCEMENT,
    CERTIFICATION_ID,
    PENDING_SURVEILLANCE,
    CORRECTIVE_ACTION_PLAN,
    COMPLAINT,
    QUARTERLY_REPORT,
    ANNUAL_REPORT;
}
