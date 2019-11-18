package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.listing.CertifiedProductSummaryEntity;

/**
 * Summary information about a listing.
 * Important things are CHPL Product Number, developer + contact info,
 * product, version, and associated URLs.
 * @author kekey
 *
 */
public class CertifiedProductSummaryDTO implements Serializable {
    private static final long serialVersionUID = 6196943224875685772L;
    private Long id;
    private String chplProductNumber;
    private String year;
    private String certificationStatus;
    private Date certificationDate;
    private CertificationBodyDTO acb;
    private DeveloperDTO developer;
    private ProductDTO product;
    private ProductVersionDTO version;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String transparencyAttestationUrl;
    private Date creationDate;
    private Date lastModifiedDate;
    private String lastModifiedUser;

    public CertifiedProductSummaryDTO() {
    }

    /**
     * Construct from entity.
     * @param entity
     */
    public CertifiedProductSummaryDTO(final CertifiedProductSummaryEntity entity) {
        this.id = entity.getId();
        this.chplProductNumber = entity.getChplProductNumber();
        this.certificationStatus = entity.getCertificationStatus();
        this.certificationDate = entity.getCertificationDate();
        this.year = entity.getYear();
        this.acb = new CertificationBodyDTO();
        this.acb.setId(entity.getCertificationBodyId());
        this.acb.setName(entity.getCertificationBodyName());
        this.developer = new DeveloperDTO();
        this.developer.setName(entity.getDeveloperName());
        ContactDTO contact = new ContactDTO();
        contact.setEmail(entity.getDeveloperContactEmail());
        contact.setPhoneNumber(entity.getDeveloperContactPhone());
        contact.setFullName(entity.getDeveloperContactName());
        this.developer.setContact(contact);
        this.product = new ProductDTO();
        this.product.setName(entity.getProductName());
        this.version = new ProductVersionDTO();
        this.version.setVersion(entity.getVersion());
        this.reportFileLocation = entity.getReportFileLocation();
        this.sedReportFileLocation = entity.getSedReportFileLocation();
        this.transparencyAttestationUrl = entity.getTransparencyAttestationUrl();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final String certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public CertificationBodyDTO getAcb() {
        return acb;
    }

    public void setAcb(final CertificationBodyDTO acb) {
        this.acb = acb;
    }

    public DeveloperDTO getDeveloper() {
        return developer;
    }

    public void setDeveloper(final DeveloperDTO developer) {
        this.developer = developer;
    }

    public ProductDTO getProduct() {
        return product;
    }

    public void setProduct(final ProductDTO product) {
        this.product = product;
    }

    public ProductVersionDTO getVersion() {
        return version;
    }

    public void setVersion(final ProductVersionDTO version) {
        this.version = version;
    }

    public String getReportFileLocation() {
        return reportFileLocation;
    }

    public void setReportFileLocation(final String reportFileLocation) {
        this.reportFileLocation = reportFileLocation;
    }

    public String getSedReportFileLocation() {
        return sedReportFileLocation;
    }

    public void setSedReportFileLocation(final String sedReportFileLocation) {
        this.sedReportFileLocation = sedReportFileLocation;
    }

    public String getTransparencyAttestationUrl() {
        return transparencyAttestationUrl;
    }

    public void setTransparencyAttestationUrl(final String transparencyAttestationUrl) {
        this.transparencyAttestationUrl = transparencyAttestationUrl;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final String lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCertificationDate() {
        return certificationDate;
    }

    public void setCertificationDate(final Date certificationDate) {
        this.certificationDate = certificationDate;
    }
}
