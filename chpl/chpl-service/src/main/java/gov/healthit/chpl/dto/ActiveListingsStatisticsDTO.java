package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.ActiveListingsStatisticsEntity;

/**
 * Active Listings data transfer object.
 * @author alarned
 *
 */
public class ActiveListingsStatisticsDTO implements Serializable {

    private static final long serialVersionUID = -1536844909545189801L;

    private Long id;
    private Long developerCount;
    private Long productCount;
    private Long certificationEditionId;
    private CertificationEditionDTO certificationEdition;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ActiveListingsStatisticsDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity IncumbentDevelopersStatisticsEntity entity
     */
    public ActiveListingsStatisticsDTO(final ActiveListingsStatisticsEntity entity) {
        this.id = entity.getId();
        this.developerCount = entity.getDeveloperCount();
        this.productCount = entity.getProductCount();
        this.certificationEditionId = entity.getCertificationEditionId();
        if (entity.getCertificationEdition() != null) {
            this.certificationEdition = new CertificationEditionDTO(entity.getCertificationEdition());
        }
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
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

    @Override
    public String toString() {
        return "Incumbent Developers Statistics DTO ["
                + "[Developer: " + this.developerCount + "]"
                + "[Product: " + this.productCount + "]"
                + "[Edition: " + this.certificationEditionId.longValue() + "]"
                + "]";
    }
}
