package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.CriterionProductStatisticsDTO;
import gov.healthit.chpl.util.Util;

/**
 * Domain object that represents criterion / product statistics used for creating charts.
 * @author alarned
 *
 */
public class CriterionProductStatistics implements Serializable {
    private static final long serialVersionUID = -1648513956784683632L;

    private Long id;
    private Long productCount;
    private Long certificationCriterionId;
    private CertificationCriterion criterion;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public CriterionProductStatistics() {
        // Default Constructor
    }

    /**
     * Constructor that populates the new object based on the dto that was passed in as a parameter.
     * @param dto CriterionProductStatisticsDTO object
     */
    public CriterionProductStatistics(final CriterionProductStatisticsDTO dto) {
        this.id = dto.getId();
        this.productCount = dto.getProductCount();
        this.certificationCriterionId = dto.getCertificationCriterionId();
        this.criterion = dto.getCriteria();
        this.deleted = dto.getDeleted();
        this.lastModifiedDate = dto.getLastModifiedDate();
        this.lastModifiedUser = dto.getLastModifiedUser();
        this.creationDate = dto.getCreationDate();
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

    public CertificationCriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(final CertificationCriterion criterion) {
        this.criterion = criterion;
    }

    public Long getCertificationCriterionId() {
        return certificationCriterionId;
    }

    public void setCertificationCriterionId(final Long certificationCriterionId) {
        this.certificationCriterionId = certificationCriterionId;
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
