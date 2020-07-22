package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

public enum QuestionableActivityTriggerConcept implements Serializable {
    CRITERIA_ADDED(
            "Certification Criteria Added"
            ),
    CRITERIA_REMOVED(
            "Certification Criteria Removed"
            ),
    CQM_ADDED(
            "CQM Added"
            ),
    CQM_REMOVED(
            "CQM Removed"
            ),
    CRITERIA_B3_ADDED_TO_EXISTING_LISTING_WITH_ICS(
            "Certification Criteria 170.315 (b)(3) added with ICS for current listing"
            ),
    G1_SUCCESS_EDITED(
            "Measure Successfully Tested for 170.314 (g)(1) Edited"
            ),
    G2_SUCCESS_EDITED(
            "Measure Successfully Tested for 170.314 (g)(2) Edited"
            ),
    G1_MEASURE_ADDED(
            "Measures Successfully Tested for 170.315 (g)(1) Added"
            ),
    G1_MEASURE_REMOVED(
            "Measures Successfully Tested for 170.315 (g)(1) Removed"
            ),
    G2_MEASURE_ADDED(
            "Measures Successfully Tested for 170.315 (g)(2) Added"
            ),
    G2_MEASURE_REMOVED(
            "Measures Successfully Tested for 170.315 (g)(2) Removed"
            ),
    GAP_EDITED(
            "GAP Status Edited"
            ),
    SURVEILLANCE_REMOVED(
            "Surveillance Removed"
            ),
    EDITION_2011_EDITED(
            "2011 Listing Edited"
            ),
    EDITION_2014_EDITED(
            "2014 Listing Edited"
            ),
    CERTIFICATION_STATUS_EDITED_CURRENT(
            "Current Certification Status Edited"
            ),
    CERTIFICATION_STATUS_DATE_EDITED_CURRENT(
            "Current Certification Date Edited"
            ),
    CERTIFICATION_STATUS_EDITED_HISTORY(
            "Historical Certification Status Edited"
            ),
    DEVELOPER_NAME_EDITED(
            "Developer Name Edited"
            ),
    DEVELOPER_STATUS_EDITED(
            "Developer Status Edited"
            ),
    DEVELOPER_STATUS_HISTORY_EDITED(
            "Developer Status History Edited"
            ),
    DEVELOPER_STATUS_HISTORY_ADDED(
            "Developer Status History Added"
            ),
    DEVELOPER_STATUS_HISTORY_REMOVED(
            "Developer Status History Removed"
            ),
    PRODUCT_NAME_EDITED(
            "Product Name Edited"
            ),
    PRODUCT_OWNER_EDITED(
            "Product Owner Edited"
            ),
    PRODUCT_OWNER_HISTORY_EDITED(
            "Product Owner History Edited"
            ),
    PRODUCT_OWNER_HISTORY_ADDED(
            "Product Owner History Added"
            ),
    PRODUCT_OWNER_HISTORY_REMOVED(
            "Product Owner History Removed"
            ),
    TESTING_LAB_CHANGED(
            "Testing Lab Changed"
            ),
    VERSION_NAME_EDITED(
            "Version Name Edited"
            ),
    REAL_WORLD_TESTING_REMOVED(
            "Real World Testing Removed"
            );

    private final String name;

    QuestionableActivityTriggerConcept(final String input) {
        this.name = input;
    }

    public static QuestionableActivityTriggerConcept findByName(final String jobTypeName) {
        QuestionableActivityTriggerConcept result = null;
        QuestionableActivityTriggerConcept[] availableValues = values();
        for (int i = 0; i < availableValues.length && result == null; i++) {
            if (availableValues[i].getName().equalsIgnoreCase(jobTypeName)
                    || availableValues[i].name().equalsIgnoreCase(jobTypeName)) {
                result = availableValues[i];
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }
}
