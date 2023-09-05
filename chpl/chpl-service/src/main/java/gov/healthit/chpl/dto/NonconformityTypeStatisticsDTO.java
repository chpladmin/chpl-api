package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.compliance.surveillance.entity.NonconformityTypeStatisticsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class NonconformityTypeStatisticsDTO {

    private Long id;
    private Long nonconformityCount;
    private String nonconformityType;
    private CertificationCriterion criterion;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public NonconformityTypeStatisticsDTO(NonconformityTypeStatisticsEntity entity) {
        this.nonconformityCount = entity.getNonconformityCount();
        this.nonconformityType = entity.getNonconformityType();
        if (entity.getCertificationCriterionEntity() != null) {
            this.criterion = entity.getCertificationCriterionEntity().toDomain();
        }
        this.id = entity.getId();
        this.setCreationDate(entity.getCreationDate());
        this.setDeleted(entity.getDeleted());
        this.setLastModifiedUser(entity.getLastModifiedUser());
        this.setLastModifiedDate(entity.getLastModifiedDate());
    }
}
