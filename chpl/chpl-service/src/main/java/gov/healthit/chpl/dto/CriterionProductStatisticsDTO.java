package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.entity.statistics.CriterionProductStatisticsEntity;
import lombok.Data;

/**
 * Represents the criterion_product_statistics table.
 * @author TYoung
 *
 */
@Data
public class CriterionProductStatisticsDTO implements Serializable {
    private static final long serialVersionUID = -8808520961352931335L;

    private Long id;
    private Long productCount;
    private Long certificationCriterionId;
    private CertificationCriterion criteria;
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
            this.criteria = entity.getCertificationCriterion().toDomain();
        }
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }
}
