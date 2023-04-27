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
    private Long certifiedProductId;
    private String chplProductNumber;
    private Long certificationBodyId;
    private String product;
    private String version;
    private String developer;
    private String certificationBody;
    private String certificationStatusName;
    private String errorMessage;
    private ZonedDateTime reportDate;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean deleted;

    public ListingValidationReport(ListingValidationReportEntity entity) {
        this.id = entity.getId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.chplProductNumber = entity.getChplProductNumber();
        this.certificationBodyId = entity.getCertificationBodyId();
        this.product = entity.getProduct();
        this.version = entity.getVersion();
        this.developer = entity.getDeveloper();
        this.certificationBody = entity.getCertificationBody();
        this.certificationStatusName = entity.getCertificationStatusName();
        this.errorMessage = entity.getErrorMessage();
        this.reportDate = entity.getReportDate();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.deleted = entity.getDeleted();
    }
}
