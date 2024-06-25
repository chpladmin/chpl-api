package gov.healthit.chpl.compliance.surveillance.entity;

import java.time.LocalDate;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.surveillance.RequirementGroupType;
import gov.healthit.chpl.domain.surveillance.RequirementType;
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

    @Basic(optional = true)
    @Column(name = "start_day")
    private LocalDate startDay;

    @Basic(optional = true)
    @Column(name = "end_day")
    private LocalDate endDay;

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
                .startDay(this.getStartDay())
                .endDay(this.getEndDay())
                .certificationEdition(this.getCertificationEdition() != null ? this.getCertificationEdition().toDomain() : null)
                .requirementGroupType(RequirementGroupType.builder()
                        .id(this.requirementGroupType.getId())
                        .name(this.requirementGroupType.getName())
                        .build())
                .build();
    }
}
