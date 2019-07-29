package gov.healthit.chpl.dto.surveillance.report;

import gov.healthit.chpl.entity.surveillance.report.QuarterlyReportExcludedListingMapEntity;

public class QuarterlyReportExclusionDTO {

    private Long id;
    private Long quarterlyReportId;
    private Long listingId;
    private String reason;

    public QuarterlyReportExclusionDTO() {
    }

    public QuarterlyReportExclusionDTO(final QuarterlyReportExcludedListingMapEntity entity) {
        this();
        this.id = entity.getId();
        this.quarterlyReportId = entity.getQuarterlyReportId();
        this.listingId = entity.getListingId();
        this.reason = entity.getReason();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getQuarterlyReportId() {
        return quarterlyReportId;
    }

    public void setQuarterlyReportId(final Long quarterlyReportId) {
        this.quarterlyReportId = quarterlyReportId;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(final Long listingId) {
        this.listingId = listingId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }
}
