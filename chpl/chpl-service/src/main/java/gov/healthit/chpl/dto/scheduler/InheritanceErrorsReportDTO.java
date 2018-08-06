package gov.healthit.chpl.dto.scheduler;

import java.io.Serializable;

import gov.healthit.chpl.entity.scheduler.InheritanceErrorsReportEntity;

/**
 * Inheritance Errors Report data transfer object.
 * @author alarned
 *
 */
public class InheritanceErrorsReportDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private String chplProductNumber;
    private String developer;
    private String product;
    private String version;
    private String acb;
    private String url;
    private String reason;
    private Boolean deleted;


    /**
     * Default constructor.
     */
    public InheritanceErrorsReportDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity InheritanceErrorsReportEntity entity
     */
    public InheritanceErrorsReportDTO(final InheritanceErrorsReportEntity entity) {
        this.id = entity.getId();
        this.chplProductNumber = entity.getChplProductNumber();
        this.developer = entity.getDeveloper();
        this.product = entity.getProduct();
        this.version = entity.getVersion();
        this.acb = entity.getAcb();
        this.url = entity.getUrl();
        this.reason = entity.getReason();
        this.deleted = entity.getDeleted();
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

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(final String developer) {
        this.developer = developer;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(final String product) {
        this.product = product;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getAcb() {
        return acb;
    }

    public void setAcb(final String acb) {
        this.acb = acb;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    @Override
    public String toString() {
        return "Inheritance Errors Report DTO ["
                + "[CHPL Product Number: " + this.chplProductNumber + "]"
                + "[Developer: " + this.developer + "]"
                + "[Product: " + this.product + "]"
                + "[Version: " + this.version + "]"
                + "[ACB: " + this.acb + "]"
                + "[URL: " + this.url + "]"
                + "[Reason: " + this.reason + "]"
                + "]";
    }
}
