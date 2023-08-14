package gov.healthit.chpl.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.criteriaattribute.rule.RuleEntity;
import gov.healthit.chpl.domain.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "certification_criterion")
public class CertificationCriterionEntity implements Serializable {
    private static final long serialVersionUID = 5366674516357955978L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_criterion_id", nullable = false)
    @JoinColumn(name = "certification_criterion_id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @Column(name = "certification_edition_id", nullable = false)
    private Long certificationEditionId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_edition_id", unique = true, nullable = true, insertable = false,
            updatable = false)
    private CertificationEditionEntity certificationEdition;

    @Basic(optional = true)
    @Column(name = "start_day")
    private LocalDate startDay;

    @Basic(optional = true)
    @Column(name = "end_day")
    private LocalDate endDay;

    @Basic(optional = true)
    @Column(name = "rule_id", nullable = false)
    private Long ruleId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id", unique = true, nullable = true, insertable = false, updatable = false)
    private RuleEntity rule;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = true)
    @Column(name = "description", length = 1000)
    private String description;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = true)
    @Column(length = 15)
    private String number;

    @Basic(optional = true)
    @Column(length = 250)
    private String title;

    @Basic(optional = true)
    @Column(name = "removed")
    private Boolean removed;

    public static CertificationCriterionEntity getNewCertificationCriterionEntity(CertificationCriterion criterion) {
        CertificationEditionEntity editionEntity = new CertificationEditionEntity();
        editionEntity.setId(criterion.getCertificationEditionId());

        return CertificationCriterionEntity.builder()
                .id(criterion.getId())
                .certificationEditionId(criterion.getCertificationEditionId())
                .certificationEdition(editionEntity)
                .startDay(criterion.getStartDay())
                .endDay(criterion.getEndDay())
                .ruleId(criterion.getRule() != null ? criterion.getRule().getId() : null)
                .description(criterion.getDescription())
                .number(criterion.getNumber())
                .title(criterion.getTitle())
                .removed(criterion.getRemoved())
                .build();
    }

    public CertificationCriterion toDomain() {
        return CertificationCriterion.builder()
                .id(this.getId())
                .certificationEdition(this.getCertificationEdition() == null ? null : this.getCertificationEdition().getYear())
                .certificationEditionId(this.getCertificationEditionId())
                .startDay(this.getStartDay())
                .endDay(this.getEndDay())
                .rule(rule != null ? rule.toDomain() : null)
                .description(this.getDescription())
                .number(this.getNumber())
                .removed(this.getRemoved())
                .title(this.getTitle())
                .build();
    }

}
