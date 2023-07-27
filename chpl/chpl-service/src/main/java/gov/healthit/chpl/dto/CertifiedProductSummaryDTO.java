package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductSummaryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Summary information about a listing.
 * Important things are CHPL Product Number, developer + contact info,
 * product, version, and associated URLs.
 * @author kekey
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CertifiedProductSummaryDTO implements Serializable {
    private static final long serialVersionUID = 6196943224875685772L;
    private Long id;
    private String chplProductNumber;
    @Deprecated
    private String year;
    private String certificationStatus;
    private Date certificationDate;
    private Boolean curesUpdate;
    private CertificationBody acb;
    private Developer developer;
    private Product product;
    private ProductVersionDTO version;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String mandatoryDisclosures;
    private Date creationDate;
    private Date lastModifiedDate;
    private String lastModifiedUser;
    private String rwtPlansUrl;
    private LocalDate rwtPlansCheckDate;
    private String rwtResultsUrl;
    private LocalDate rwtResultsCheckDate;
    private Integer rwtEligibilityYear;


    /**
     * Construct from entity.
     * @param entity
     */
    public CertifiedProductSummaryDTO(CertifiedProductSummaryEntity entity) {
        this.id = entity.getId();
        this.chplProductNumber = entity.getChplProductNumber();
        this.certificationStatus = entity.getCertificationStatus();
        this.certificationDate = entity.getCertificationDate();
        this.curesUpdate = entity.getCuresUpdate();
        this.year = entity.getYear();
        this.acb = CertificationBody.builder()
                .id(entity.getCertificationBodyId())
                .name(entity.getCertificationBodyName())
                .acbCode(entity.getAcbCode())
                .website(entity.getCertificationBodyWebsite())
                .build();
        this.developer = new Developer();
        this.developer.setName(entity.getDeveloperName());
        PointOfContact contact = new PointOfContact();
        contact.setEmail(entity.getDeveloperContactEmail());
        contact.setPhoneNumber(entity.getDeveloperContactPhone());
        contact.setFullName(entity.getDeveloperContactName());
        this.developer.setContact(contact);
        this.product = new Product();
        this.product.setName(entity.getProductName());
        this.version = new ProductVersionDTO();
        this.version.setVersion(entity.getVersion());
        this.reportFileLocation = entity.getReportFileLocation();
        this.sedReportFileLocation = entity.getSedReportFileLocation();
        this.mandatoryDisclosures = entity.getMandatoryDisclosures();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.rwtPlansUrl = entity.getRwtPlansUrl();
        this.rwtPlansCheckDate = entity.getRwtPlansCheckDate();
        this.rwtResultsUrl = entity.getRwtResultsUrl();
        this.rwtResultsCheckDate = entity.getRwtResultsCheckDate();
    }

    public CertifiedProductSummaryDTO(CertifiedProductDetailsEntity entity) {
        this.id = entity.getId();
        this.chplProductNumber = entity.getChplProductNumber();
        this.certificationStatus = entity.getCertificationStatusName();
        this.certificationDate = entity.getCertificationDate();
        this.curesUpdate = entity.getCuresUpdate();
        this.year = entity.getYear();
        this.acb = CertificationBody.builder()
                .id(entity.getCertificationBodyId())
                .name(entity.getCertificationBodyName())
                .acbCode(entity.getCertificationBodyCode())
                .retired(entity.getAcbIsRetired())
                .build();
        this.developer = new Developer();
        this.developer.setId(entity.getDeveloperId());
        this.developer.setName(entity.getDeveloperName());
        PointOfContact contact = new PointOfContact();
        contact.setEmail(entity.getEmail());
        contact.setPhoneNumber(entity.getPhoneNumber());
        contact.setFullName(entity.getFullName());
        this.developer.setContact(contact);
        this.product = new Product();
        this.product.setId(entity.getProductId());
        this.product.setName(entity.getProductName());
        this.version = new ProductVersionDTO();
        this.version.setId(entity.getProductVersionId());
        this.version.setVersion(entity.getProductVersion());
        this.reportFileLocation = entity.getReportFileLocation();
        this.sedReportFileLocation = entity.getSedReportFileLocation();
        this.mandatoryDisclosures = entity.getMandatoryDisclosures();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.rwtPlansUrl = entity.getRwtPlansUrl();
        this.rwtPlansCheckDate = entity.getRwtPlansCheckDate();
        this.rwtResultsUrl = entity.getRwtResultsUrl();
        this.rwtResultsCheckDate = entity.getRwtResultsCheckDate();
    }
}
