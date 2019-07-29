package gov.healthit.chpl.dto.surveillance.report;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;

public class QuarterlyReportRelevantListingDTO extends CertifiedProductDetailsDTO {
    private static final long serialVersionUID = -2198910382314894675L;
    private boolean isExcluded;
    private String exclusionReason;

    public QuarterlyReportRelevantListingDTO() {
        super();
    }

    public QuarterlyReportRelevantListingDTO(CertifiedProductDetailsEntity entity) {
        super(entity);
    }

    public boolean isExcluded() {
        return isExcluded;
    }

    public void setExcluded(final boolean isExcluded) {
        this.isExcluded = isExcluded;
    }

    public String getExclusionReason() {
        return exclusionReason;
    }

    public void setExclusionReason(final String exclusionReason) {
        this.exclusionReason = exclusionReason;
    }

}
