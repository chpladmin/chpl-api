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
