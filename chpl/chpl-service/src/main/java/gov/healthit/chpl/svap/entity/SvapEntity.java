package gov.healthit.chpl.svap.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.WhereJoinTable;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.svap.domain.Svap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "svap")
public class SvapEntity extends EntityAudit {
    private static final long serialVersionUID = -38745571925191621L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long svapId;

    @Column(name = "regulatory_text_citation", nullable = false)
    private String regulatoryTextCitation;

    @Column(name = "approved_standard_version", nullable = false)
    private String approvedStandardVersion;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "svap_criteria_map",
        joinColumns = {@JoinColumn(name = "svap_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "criteria_id", referencedColumnName = "certification_criterion_id")})
    @WhereJoinTable(clause = "deleted <> true")
    private List<CertificationCriterionEntity> criteria;

    @Column(name = "replaced", nullable = false)
    private Boolean replaced;

    public Svap toDomain() {
        return Svap.builder()
                .svapId(this.getSvapId())
                .approvedStandardVersion(this.getApprovedStandardVersion())
                .regulatoryTextCitation(this.getRegulatoryTextCitation())
                .replaced(this.getReplaced())
                .build();
    }

    public Svap toDomainWithCriteria(CertificationCriterionComparator criterionComparator) {
        return Svap.builder()
                .svapId(this.getSvapId())
                .approvedStandardVersion(this.getApprovedStandardVersion())
                .regulatoryTextCitation(this.getRegulatoryTextCitation())
                .replaced(this.getReplaced())
                .criteria(this.getCriteria().stream()
                        .map(crit -> crit.toDomain())
                        .sorted(criterionComparator)
                        .collect(Collectors.toCollection(ArrayList::new)))
                .build();
    }

}
