package gov.healthit.chpl.surveillance.report.dto;

import gov.healthit.chpl.surveillance.report.entity.QuarterlyReportExcludedListingMapEntity;
import lombok.Data;

@Data
public class QuarterlyReportExclusionDTO {

    private Long id;
    private Long quarterlyReportId;
    private Long listingId;
    private String reason;

    public QuarterlyReportExclusionDTO() {
    }

    public QuarterlyReportExclusionDTO(QuarterlyReportExcludedListingMapEntity entity) {
        this();
        this.id = entity.getId();
        this.quarterlyReportId = entity.getQuarterlyReportId();
        this.listingId = entity.getListingId();
        this.reason = entity.getReason();
    }
}
