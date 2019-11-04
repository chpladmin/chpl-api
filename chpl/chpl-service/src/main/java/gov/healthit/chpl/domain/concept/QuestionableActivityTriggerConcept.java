package gov.healthit.chpl.domain.concept;

import java.io.Serializable;

/**
 * Concepts for triggering Questionable Activity.
 * @author alarned
 *
 */
public enum QuestionableActivityTriggerConcept implements Serializable {
    /**
     * Certification Criteria Added.
     */
    CRITERIA_ADDED("Certification Criteria Added"),
    /**
     * Certification Criteria Removed.
     */
    CRITERIA_REMOVED("Certification Criteria Removed"),
    /**
     * CQM Added.
     */
    CQM_ADDED("CQM Added"),
    /**
     * CQM Removed.
     */
    CQM_REMOVED("CQM Removed"),
    /**
     * Measure Successfully Tested for 170.314 (g)(1) Edited.
     */
    G1_SUCCESS_EDITED("Measure Successfully Tested for 170.314 (g)(1) Edited"),
    /**
     * Measure Successfully Tested for 170.314 (g)(2) Edited.
     */
    G2_SUCCESS_EDITED("Measure Successfully Tested for 170.314 (g)(2) Edited"),
    /**
     * Measures Successfully Tested for 170.315 (g)(1) Added.
     */
    G1_MEASURE_ADDED("Measures Successfully Tested for 170.315 (g)(1) Added"),
    /**
     * Measures Successfully Tested for 170.315 (g)(1) Removed.
     */
    G1_MEASURE_REMOVED("Measures Successfully Tested for 170.315 (g)(1) Removed"),
    /**
     * Measures Successfully Tested for 170.315 (g)(2) Added.
     */
    G2_MEASURE_ADDED("Measures Successfully Tested for 170.315 (g)(2) Added"),
    /**
     * Measures Successfully Tested for 170.315 (g)(2) Removed.
     */
    G2_MEASURE_REMOVED("Measures Successfully Tested for 170.315 (g)(2) Removed"),
    /**
     * GAP Status Edited.
     */
    GAP_EDITED("GAP Status Edited"),
    /**
     * Surveillance Removed.
     */
    SURVEILLANCE_REMOVED("Surveillance Removed"),
    /**
     * 2011 Listing Edited.
     */
    EDITION_2011_EDITED("2011 Listing Edited"),
    /**
     * 2014 Listing Edited.
     */
    EDITION_2014_EDITED("2014 Listing Edited"),
    /**
     * Current Certification Status Edited.
     */
    CERTIFICATION_STATUS_EDITED_CURRENT("Current Certification Status Edited"),
    /**
     * Current Certification Date Edited.
     */
    CERTIFICATION_STATUS_DATE_EDITED_CURRENT("Current Certification Date Edited"),
    /**
     * Historical Certification Status Edited.
     */
    CERTIFICATION_STATUS_EDITED_HISTORY("Historical Certification Status Edited"),
    /**
     * Developer Name Edited.
     */
    DEVELOPER_NAME_EDITED("Developer Name Edited"),
    /**
     * Developer Status Edited.
     */
    DEVELOPER_STATUS_EDITED("Developer Status Edited"),
    /**
     * Developer Status History Edited.
     */
    DEVELOPER_STATUS_HISTORY_EDITED("Developer Status History Edited"),
    /**
     * Developer Status History Added.
     */
    DEVELOPER_STATUS_HISTORY_ADDED("Developer Status History Added"),
    /**
     * Developer Status History Removed.
     */
    DEVELOPER_STATUS_HISTORY_REMOVED("Developer Status History Removed"),
    /**
     * Product Name Edited.
     */
    PRODUCT_NAME_EDITED("Product Name Edited"),
    /**
     * Product Owner Edited.
     */
    PRODUCT_OWNER_EDITED("Product Owner Edited"),
    /**
     * Product Owner History Edited.
     */
    PRODUCT_OWNER_HISTORY_EDITED("Product Owner History Edited"),
    /**
     * Product Owner History Added.
     */
    PRODUCT_OWNER_HISTORY_ADDED("Product Owner History Added"),
    /**
     * Product Owner History Removed.
     */
    PRODUCT_OWNER_HISTORY_REMOVED("Product Owner History Removed"),
    /**
     * Testing Lab Changed.
     */
    TESTING_LAB_CHANGED("Testing Lab Changed"),
    /**
     * Version Name Edited.
     */
    VERSION_NAME_EDITED("Version Name Edited");

    private final String name;

    QuestionableActivityTriggerConcept(final String input) {
        this.name = input;
    }

    /**
     * Find Questionable Activity Trigger Concept.
     * @param jobTypeName job type to find
     * @return result of search
     */
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
