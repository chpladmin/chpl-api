package gov.healthit.chpl.scheduler.job.listingvalidation;

import java.time.ZonedDateTime;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ListingValidationReport {
    private Long id;
    private String chplProductNumber;
    private Long certificationBodyId;
    private String productName;
    private String certificationStatusName;
    private String errorMessage;
    private ZonedDateTime reportDate;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public ListingValidationReport(ListingValidationReportEntity entity) {
        this.id = entity.getId();
        this.chplProductNumber = entity.getChplProductNumber();
        this.certificationBodyId = entity.getCertificationBodyId();
        this.productName = entity.getProductName();
        this.certificationStatusName = entity.getCertificationStatusName();
        this.errorMessage = entity.getErrorMessage();
        this.reportDate = entity.getReportDate();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();
    }
}
