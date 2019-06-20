package gov.healthit.chpl.domain.surveillance.report;

import java.io.Serializable;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportRelevantListingDTO;

public class RelevantListing extends CertifiedProduct implements Serializable {
    private static final long serialVersionUID = -4490178928672550687L;

    private boolean isExcluded = false;
    private String reason;

    public RelevantListing() {
        super();
    }

    public RelevantListing(final QuarterlyReportRelevantListingDTO dto) {
        super(dto);
        this.isExcluded = dto.isExcluded();
        this.reason = dto.getExclusionReason();
    }

    public boolean isExcluded() {
        return isExcluded;
    }

    public void setExcluded(final boolean isExcluded) {
        this.isExcluded = isExcluded;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }
}
