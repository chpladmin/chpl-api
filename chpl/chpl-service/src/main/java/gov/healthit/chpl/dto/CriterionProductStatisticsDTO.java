package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.statistics.CriterionProductStatisticsEntity;
import gov.healthit.chpl.util.Util;

/**
 * Represents the criterion_product_statistics table.
 * @author TYoung
 *
 */
public class CriterionProductStatisticsDTO implements Serializable {
    private static final long serialVersionUID = -8808520961352931335L;

    private Long id;
    private Long productCount;
    private Long certificationCriterionId;
    private CertificationCriterionDTO criteria;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public CriterionProductStatisticsDTO() {
    }

    /**
     * Constructor that will populate the created object based on the entity
     * that is passed in as a parameter.
     * @param entity CriterionProductStatisticsEntity entity
     */
    public CriterionProductStatisticsDTO(final CriterionProductStatisticsEntity entity) {
        this.setId(entity.getId());
        this.setProductCount(entity.getProductCount());
        this.setCertificationCriterionId(entity.getCertificationCriterionId());
        if (entity.getCertificationCriterion() != null) {
            this.criteria = new CertificationCriterionDTO(entity.getCertificationCriterion());
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

    public Long getProductCount() {
        return productCount;
    }

    public void setProductCount(final Long productCount) {
        this.productCount = productCount;
    }

    public Long getCertificationCriterionId() {
        return certificationCriterionId;
    }

    public void setCertificationCriterionId(final Long certificationCriterionId) {
        this.certificationCriterionId = certificationCriterionId;
    }

    public CertificationCriterionDTO getCriteria() {
        return criteria;
    }

    public void setCriteria(final CertificationCriterionDTO criteria) {
        this.criteria = criteria;
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

}
