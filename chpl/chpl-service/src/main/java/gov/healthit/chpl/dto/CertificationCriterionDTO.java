package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertificationCriterionDTO implements Serializable {
    private static final long serialVersionUID = -1129602624256345286L;
    private Long id;
    private Boolean automatedMeasureCapable;
    private Boolean automatedNumeratorCapable;
    private Long certificationEditionId;
    private String certificationEdition;
    private Date creationDate;
    private Boolean deleted;
    private String description;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private String number;
    private Boolean requiresSed;
    private String title;
    private Boolean removed;

    public CertificationCriterionDTO(final CertificationCriterionEntity entity) {
        this.id = entity.getId();
        this.automatedMeasureCapable = entity.getAutomatedMeasureCapable();
        this.automatedNumeratorCapable = entity.getAutomatedNumeratorCapable();
        this.certificationEditionId = entity.getCertificationEditionId();
        if (entity.getCertificationEdition() != null) {
            this.certificationEdition = entity.getCertificationEdition().getYear();
        }
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.description = entity.getDescription();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.number = entity.getNumber();

        this.requiresSed = entity.getRequiresSed();
        this.title = entity.getTitle();
        this.removed = entity.getRemoved();
    }

    public CertificationCriterionDTO(final CertificationCriterion domain) {

        this.id = domain.getId();
        this.certificationEditionId = domain.getCertificationEditionId();
        if (domain.getCertificationEdition() != null) {
            this.certificationEdition = domain.getCertificationEdition();
        }
        this.description = domain.getDescription();
        this.number = domain.getNumber();
        this.title = domain.getTitle();
        this.removed = domain.getRemoved();
    }

}
