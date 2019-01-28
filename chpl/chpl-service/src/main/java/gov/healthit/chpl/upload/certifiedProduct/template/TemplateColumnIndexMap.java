package gov.healthit.chpl.upload.certifiedProduct.template;

import org.apache.commons.csv.CSVRecord;

public abstract class TemplateColumnIndexMap {
    protected static final String CRITERIA_COL_HEADING_BEGIN = "CRITERIA_";

    public abstract int getUniqueIdIndex();
    public abstract int getRecordStatusIndex();
    public abstract int getPracticeTypeIndex();
    public abstract int getDeveloperIndex();
    public abstract int getProductIndex();
    public abstract int getVersionIndex();
    public abstract int getEditionIndex();
    public abstract int getAcbCertificationIdIndex();
    public abstract int getAcbIndex();
    public abstract int getAtlIndex();
    public abstract int getProductClassificationIndex();
    public abstract int getCertificationDateIndex();
    public abstract int getDeveloperAddressStartIndex();
    public abstract int getDeveloperAddressEndIndex();
    public abstract int getTargetedUserStartIndex();
    public abstract int getTargetedUserEndIndex();
    public abstract int getQmsStartIndex();
    public abstract int getQmsEndIndex();
    public abstract int getIcsStartIndex();
    public abstract int getIcsEndIndex();
    public abstract int getAccessibilityCertifiedIndex();
    public abstract int getAccessibilityStandardIndex();
    public abstract int getK1Index();
    public abstract int getK2Index();
    public abstract int getCqmStartIndex();
    public abstract int getCqmEndIndex();
    public abstract int getSedStartIndex();
    public abstract int getSedEndIndex();
    public abstract int getTestParticipantStartIndex();
    public abstract int getTestParticipantEndIndex();
    public abstract int getTestTaskStartIndex();
    public abstract int getTestTaskEndIndex();
    public abstract int getCriteriaStartIndex();
    public abstract int getCriteriaEndIndex();

    public String getGapColumnLabel() {
        return "GAP";
    }

    public int getGapColumnCount() {
        return 1;
    }

    public String getPrivacySecurityFrameworkColumnLabel() {
        return "PRIVACY AND SECURITY FRAMEWORK";
    }

    public int getPrivacySecurityFrameworkColumnCount() {
        return 1;
    }

    public String getApiDocumentationColumnLabel() {
        return "API DOCUMENTATION LINK";
    }

    public int getApiDocumentationColumnCount() {
        return 1;
    }

    public String getTestStandardsColumnLabel() {
        return "STANDARD TESTED AGAINST";
    }

    public int getTestStandardsColumnCount() {
        return 1;
    }

    public String getTestFunctionalityColumnLabel() {
        return "FUNCTIONALITY TESTED";
    }

    public int getTestFunctionalityColumnCount() {
        return 1;
    }

    public String getG1MeasureColumnLabel() {
        return "MEASURE SUCCESSFULLY TESTED FOR G1";
    }

    public int getG1MeasureColumnCount() {
        return 1;
    }

    public String getG2MeasureColumnLabel() {
        return "MEASURE SUCCESSFULLY TESTED FOR G2";
    }

    public int getG2MeasureColumnCount() {
        return 1;
    }

    public String getAdditionalSoftwareColumnLabel() {
        return "ADDITIONAL SOFTWARE";
    }

    public int getAdditionalSoftwareColumnCount() {
        return 6;
    }

    public String getTestToolColumnLabel() {
        return "TEST TOOL NAME";
    }

    public int getTestToolColumnCount() {
        return 2;
    }

    public String getTestProcedureColumnLabel() {
        return "TEST PROCEDURE";
    }

    public int getTestProcedureColumnCount() {
        return 1;
    }

    public String getTestProcedureVersionColumnLabel() {
        return "TEST PROCEDURE VERSION";
    }

    public int getTestProcedureVersionColumnCount() {
        return 1;
    }

    public String getTestDataColumnLabel() {
        return  "TEST DATA VERSION";
    }

    public int getTestDataColumnCount() {
        return 3;
    }

    public String getUcdColumnLabel() {
        return "UCD PROCESS SELECTED";
    }

    public int getUcdColumnCount() {
        return 2;
    }

    public String getTestTasksColumnLabel() {
        return "TASK IDENTIFIER";
    }

    public int getTestTasksColumnCount() {
        return 2;
    }

    public int getLastIndexForCriteria(CSVRecord heading, int beginIndex) {
        int criteriaBeginIndex = beginIndex;
        int criteriaEndIndex = criteriaBeginIndex + 1;
        String colTitle = heading.get(criteriaBeginIndex).toString();
        if (colTitle.startsWith(CRITERIA_COL_HEADING_BEGIN)) {
            colTitle = heading.get(criteriaEndIndex).toString();
            while (criteriaEndIndex <= getCriteriaEndIndex() && !colTitle.startsWith(CRITERIA_COL_HEADING_BEGIN)) {
                criteriaEndIndex++;
                if (criteriaEndIndex <= getCriteriaEndIndex()) {
                    colTitle = heading.get(criteriaEndIndex).toString();
                }
            }
        } else {
            return -1;
        }
        return criteriaEndIndex - 1;
    }
}
