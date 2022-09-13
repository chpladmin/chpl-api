package gov.healthit.chpl.entity.surveillance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.surveillance.RequirementDetailType;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
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
@Table(name = "requirement_detail_type")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementDetailTypeEntity {

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
    @JoinColumn(name = "surveillance_requirement_type_id", insertable = false, updatable = false)
    private SurveillanceRequirementTypeEntity surveillanceRequirementType;

    public RequirementDetailType toDomain() {
        return RequirementDetailType.builder()
                .id(this.id)
                .number(this.number)
                .title(this.title)
                .removed(this.removed)
                .certificationEdition(this.getCertificationEdition() != null ? new CertificationEdition(new CertificationEditionDTO(this.getCertificationEdition())) : null)
                .surveillanceRequirementType(SurveillanceRequirementType.builder()
                        .id(this.surveillanceRequirementType.getId())
                        .name(this.surveillanceRequirementType.getName())
                        .build())
                .build();
    }
}
