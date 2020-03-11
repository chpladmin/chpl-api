package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.surveillance.NonconformityTypeStatisticsEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public @Data class NonconformityTypeStatisticsDTO {

    private Long id;
    private Long nonconformityCount;
    private String nonconformityType;
    private CertificationCriterionDTO criterion;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public NonconformityTypeStatisticsDTO(NonconformityTypeStatisticsEntity entity) {
        this.nonconformityCount = entity.getNonconformityCount();
        this.nonconformityType = entity.getNonconformityType();
        if (entity.getCertificationCriterionEntity() != null) {
            this.criterion = new CertificationCriterionDTO(entity.getCertificationCriterionEntity());
        }
        this.id = entity.getId();
        this.setCreationDate(entity.getCreationDate());
        this.setDeleted(entity.getDeleted());
        this.setLastModifiedUser(entity.getLastModifiedUser());
        this.setLastModifiedDate(entity.getLastModifiedDate());
    }
}
