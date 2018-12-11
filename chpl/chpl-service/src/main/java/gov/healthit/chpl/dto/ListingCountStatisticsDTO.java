package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.ListingCountStatisticsEntity;
import gov.healthit.chpl.util.Util;

/**
 * Listing Count data transfer object.
 * @author alarned
 *
 */
public class ListingCountStatisticsDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private Long developerCount;
    private Long productCount;
    private Long certificationEditionId;
    private CertificationEditionDTO certificationEdition;
    private Long certificationStatusId;
    private CertificationStatusDTO certificationStatus;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ListingCountStatisticsDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity IncumbentDevelopersStatisticsEntity entity
     */
    public ListingCountStatisticsDTO(final ListingCountStatisticsEntity entity) {
        this.id = entity.getId();
        this.developerCount = entity.getDeveloperCount();
        this.productCount = entity.getProductCount();
        this.certificationEditionId = entity.getCertificationEditionId();
        if (entity.getCertificationEdition() != null) {
            this.certificationEdition = new CertificationEditionDTO(entity.getCertificationEdition());
        }
        this.certificationStatusId = entity.getCertificationStatusId();
        if (entity.getCertificationStatus() != null) {
            this.certificationStatus = new CertificationStatusDTO(entity.getCertificationStatus());
        }
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }

    /**
     * New DTO with edition and status; sets counts to 0.
     * @param edition the edition
     * @param status the status
     */
    public ListingCountStatisticsDTO(final CertificationEditionDTO edition, final CertificationStatusDTO status) {
        this.developerCount = 0L;
        this.productCount = 0L;
        this.certificationEditionId = edition.getId();
        this.certificationStatusId = status.getId();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Long getDeveloperCount() {
        return developerCount;
    }

    public void setDeveloperCount(final Long developerCount) {
        this.developerCount = developerCount;
    }

    public Long getProductCount() {
        return productCount;
    }

    public void setProductCount(final Long productCount) {
        this.productCount = productCount;
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public CertificationEditionDTO getCertificationEdition() {
        return certificationEdition;
    }

    public void setCertificationEdition(final CertificationEditionDTO certificationEdition) {
        this.certificationEdition = certificationEdition;
    }

    public Long getCertificationStatusId() {
        return certificationStatusId;
    }

    public void setCertificationStatusId(final Long certificationStatusId) {
        this.certificationStatusId = certificationStatusId;
    }

    public CertificationStatusDTO getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final CertificationStatusDTO certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    @Override
    public String toString() {
        return "Listing Count Statistics DTO ["
                + "[Developer: " + this.developerCount + "]"
                + "[Product: " + this.productCount + "]"
                + "[Edition: " + this.certificationEditionId.longValue() + "]"
                + "[Status: " + this.certificationStatusId.longValue() + "]"
                + "]";
    }
}
