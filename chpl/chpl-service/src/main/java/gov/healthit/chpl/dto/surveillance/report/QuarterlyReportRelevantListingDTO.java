package gov.healthit.chpl.dto.surveillance.report;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;

public class QuarterlyReportRelevantListingDTO extends CertifiedProductDetailsDTO {
    private static final long serialVersionUID = -2198910382314894675L;
    private boolean isExcluded;
    private String exclusionReason;
    private List<QuarterlyReportSurveillanceMapDTO> surveillances;

    public QuarterlyReportRelevantListingDTO() {
        super();
        this.surveillances = new ArrayList<QuarterlyReportSurveillanceMapDTO>();
    }

    public QuarterlyReportRelevantListingDTO(final CertifiedProductDetailsEntity entity) {
        super(entity);
        this.surveillances = new ArrayList<QuarterlyReportSurveillanceMapDTO>();
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

    public List<QuarterlyReportSurveillanceMapDTO> getSurveillances() {
        return surveillances;
    }

    public void setSurveillances(final List<QuarterlyReportSurveillanceMapDTO> surveillances) {
        this.surveillances = surveillances;
    }

    @Override
    public boolean equals(final Object anotherObject) {
        if (anotherObject == null || !(anotherObject instanceof QuarterlyReportRelevantListingDTO)) {
            return false;
        }
        QuarterlyReportRelevantListingDTO anotherRelevantListing = (QuarterlyReportRelevantListingDTO) anotherObject;
        if (this.getId() == null && anotherRelevantListing.getId() != null
                || this.getId() != null && anotherRelevantListing.getId() == null
                || this.getId() == null && anotherRelevantListing.getId() == null) {
            return false;
        }
        if (this.getId().longValue() == anotherRelevantListing.getId().longValue()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (this.getId() == null) {
            return -1;
        }
        return this.getId().hashCode();
    }
}
