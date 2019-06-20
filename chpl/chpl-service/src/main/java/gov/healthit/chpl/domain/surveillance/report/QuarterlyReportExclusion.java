package gov.healthit.chpl.domain.surveillance.report;

import java.io.Serializable;

import gov.healthit.chpl.dto.surveillance.report.QuarterlyReportExclusionDTO;

public class QuarterlyReportExclusion implements Serializable {
    private static final long serialVersionUID = 8743499678379559105L;

    private Long id;
    private Long listingId;
    private String reason;

    public QuarterlyReportExclusion() {
    }

    public QuarterlyReportExclusion(final QuarterlyReportExclusionDTO dto) {
        this.id = dto.getId();
        this.listingId = dto.getListingId();
        this.reason = dto.getReason();
    }

    public Long getId() {
        return id;
    }
    public void setId(final Long id) {
        this.id = id;
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
