package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.dto.CriterionProductStatisticsDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
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
}
