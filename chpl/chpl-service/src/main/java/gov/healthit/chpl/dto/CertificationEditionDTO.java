package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.CertificationEditionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationEditionDTO implements Serializable {
    private static final long serialVersionUID = -2554595626818018414L;
    private Long id;
    @Builder.Default
    private Set<CertificationCriterionDTO> certificationCriterions = new HashSet<CertificationCriterionDTO>();
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String year;
    private Boolean retired;

    public CertificationEditionDTO(CertificationEditionEntity entity) {
        this.id = entity.getId();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.isDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.year = entity.getYear();
        this.retired = entity.getRetired();
        Set<CertificationCriterionEntity> certCriterionEntities = entity.getCertificationCriterions();
        if (certCriterionEntities != null && certCriterionEntities.size() > 0) {
            for (CertificationCriterionEntity certCriterion : certCriterionEntities) {
                CertificationCriterionDTO ccDto = new CertificationCriterionDTO(certCriterion);
                this.certificationCriterions.add(ccDto);
            }
        }
    }
}
