package gov.healthit.chpl.report.criteriauptodate;

import org.apache.logging.log4j.Logger;

import gov.healthit.chpl.attribute.AttributeUpToDate;
import gov.healthit.chpl.attribute.CodeSetUpToDate;
import gov.healthit.chpl.attribute.FunctionalityTestedUpToDate;
import gov.healthit.chpl.attribute.StandardGroupUpToDate;
import gov.healthit.chpl.attribute.StandardUpToDate;
import lombok.Getter;

public enum CriterionNotUpToDateReasonEnum {
    STANDARD_ATTESTED("Standard Attested"),
    REQUIRED_STANDARD_NOT_ATTESTED("Required Standard Not Attested"),
    REQUIRED_STANDARD_FROM_GROUP_NOT_ATTESTED("Required Standard From Group Not Attested"),
    FUNCTIONALITY_TESTED_ATTESTED("Functionality Tested Attested"),
    REQUIRED_FUNCTIONALITY_TESTED_NOT_ATTESTED("Required Functionality Tested Not Attested"),
    CODE_SET_ATTESTED("Code Set Attested"),
    REQUIRED_CODE_SET_NOT_ATTESTED("Required Code Set Not Attested");

    @Getter
    private String name;
    CriterionNotUpToDateReasonEnum(String name) {
        this.name = name;
    }

    public static CriterionNotUpToDateReasonEnum calculateReason(AttributeUpToDate attributeUpToDate, Logger logger) {
        if (attributeUpToDate.getExpiringButPresent()) {
            if (attributeUpToDate instanceof StandardUpToDate) {
                return STANDARD_ATTESTED;
            } else if (attributeUpToDate instanceof FunctionalityTestedUpToDate) {
                return FUNCTIONALITY_TESTED_ATTESTED;
            } else if (attributeUpToDate instanceof CodeSetUpToDate) {
                return CODE_SET_ATTESTED;
            }
        } else if (attributeUpToDate.getRequiredButNotPresent()) {
            if (attributeUpToDate instanceof StandardUpToDate) {
                return REQUIRED_STANDARD_NOT_ATTESTED;
            } else if (attributeUpToDate instanceof StandardGroupUpToDate) {
                return REQUIRED_STANDARD_FROM_GROUP_NOT_ATTESTED;
            } else if (attributeUpToDate instanceof FunctionalityTestedUpToDate) {
                return REQUIRED_FUNCTIONALITY_TESTED_NOT_ATTESTED;
            } else if (attributeUpToDate instanceof CodeSetUpToDate) {
                return REQUIRED_CODE_SET_NOT_ATTESTED;
            }
        }
        logger.error("No Criterion Up-To-Date Reason was found for " + attributeUpToDate.getClass().getName());
        return null;
    }
}
