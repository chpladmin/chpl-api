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
    LISTING_UPLOAD,
    API_KEY,
    ANNOUNCEMENT,
    CERTIFICATION_ID,
    PENDING_SURVEILLANCE,
    CORRECTIVE_ACTION_PLAN,
    COMPLAINT,
    QUARTERLY_REPORT,
    QUARTERLY_REPORT_LISTING,
    ANNUAL_REPORT,
    CHANGE_REQUEST;
}
