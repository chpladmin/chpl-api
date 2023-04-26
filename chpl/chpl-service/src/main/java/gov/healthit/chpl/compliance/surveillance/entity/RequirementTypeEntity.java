package gov.healthit.chpl.compliance.surveillance.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.surveillance.RequirementGroupType;
import gov.healthit.chpl.domain.surveillance.RequirementType;
import gov.healthit.chpl.dto.CertificationEditionDTO;
import gov.healthit.chpl.entity.CertificationEditionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "requirement_type")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "number")
    private String number;

    @Column(name = "title")
    private String title;

    @Column(name = "removed")
    private Boolean removed;

    @OneToOne(optional = true)
    @JoinColumn(name = "certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity certificationEdition;

    @OneToOne(optional = true)
    @JoinColumn(name = "requirement_group_type_id", insertable = false, updatable = false)
    private RequirementGroupTypeEntity requirementGroupType;

    public RequirementType toDomain() {
        return RequirementType.builder()
                .id(this.id)
                .number(this.number)
                .title(this.title)
                .removed(this.removed)
                .certificationEdition(this.getCertificationEdition() != null ? new CertificationEdition(new CertificationEditionDTO(this.getCertificationEdition())) : null)
                .requirementGroupType(RequirementGroupType.builder()
                        .id(this.requirementGroupType.getId())
                        .name(this.requirementGroupType.getName())
                        .build())
                .build();
    }
}
