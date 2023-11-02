package gov.healthit.chpl.questionableactivity;

import java.io.Serializable;

public enum QuestionableActivityTriggerConcept implements Serializable {
    CRITERIA_REMOVED("Certification Criteria Removed"),
    CQM_ADDED("CQM Added"),
    CQM_REMOVED("CQM Removed"),
    G1_SUCCESS_EDITED("Measure Successfully Tested for 170.314 (g)(1) Edited"),
    G2_SUCCESS_EDITED("Measure Successfully Tested for 170.314 (g)(2) Edited"),
    MEASURE_REMOVED("G1/G2 Removed"),
    GAP_EDITED("GAP Status Edited"),
    SURVEILLANCE_REMOVED("Surveillance Removed"),
    CERTIFICATION_DATE_EDITED("Certification Date Edited"),
    CERTIFICATION_STATUS_EDITED_CURRENT("Current Certification Status Edited"),
    CERTIFICATION_STATUS_EDITED_HISTORY("Historical Certification Status Edited"),
    DEVELOPER_NAME_EDITED("Developer Name Edited"),
    DEVELOPER_STATUS_EDITED("Developer Status Edited"),
    DEVELOPER_STATUS_HISTORY_EDITED("Developer Status History Edited"),
    DEVELOPER_STATUS_HISTORY_ADDED("Developer Status History Added"),
    DEVELOPER_STATUS_HISTORY_REMOVED("Developer Status History Removed"),
    PRODUCT_NAME_EDITED("Product Name Edited"),
    PRODUCT_OWNER_EDITED("Product Owner Edited"),
    PRODUCT_OWNER_HISTORY_EDITED("Product Owner History Edited"),
    PRODUCT_OWNER_HISTORY_ADDED("Product Owner History Added"),
    PRODUCT_OWNER_HISTORY_REMOVED("Product Owner History Removed"),
    TESTING_LAB_CHANGED("Testing Lab Changed"),
    VERSION_NAME_EDITED("Version Name Edited"),
    REAL_WORLD_TESTING_REMOVED("Real World Testing Removed"),
    REAL_WORLD_TESTING_ADDED("Real World Testing Added To Ineligible Listing"),
    REPLACED_SVAP_ADDED("Replaced SVAP Added"),
    PROMOTING_INTEROPERABILITY_UPDATED_BY_ACB("Promoting Interoperability Updated by ONC-ACB"),
    RWT_PLANS_UPDATED_OUTSIDE_NORMAL_PERIOD("Real World Testing Plans URL or Check Date updated outside normal update period"),
    RWT_RESULTS_UPDATED_OUTSIDE_NORMAL_PERIOD("Real World Testing Results URL or Check Date updated outside normal update period"),
    NON_ACTIVE_CERTIFIFCATE_EDITED("Non Active Certificate Edited"),

    //These are no longer being detected but there may be existing older questionable activities
    EDITION_2011_EDITED("2011 Listing Edited"),
    EDITION_2014_EDITED("2014 Listing Edited"),
    CERTIFICATION_STATUS_DATE_EDITED_CURRENT("Current Certification Status Date Edited"),
    CURES_UPDATE_REMOVED("Cures Update Designation Removed");

    private final String name;

    QuestionableActivityTriggerConcept(String input) {
        this.name = input;
    }

    public static QuestionableActivityTriggerConcept findByName(String jobTypeName) {
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
