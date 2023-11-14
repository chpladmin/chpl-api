package gov.healthit.chpl.functionalitytested;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.criteriaattribute.rule.RuleEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.PracticeTypeEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "functionality_tested")
public class FunctionalityTestedEntity extends EntityAudit implements Serializable {
    private static final long serialVersionUID = 2662883108826795645L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "number")
    private String number;

    @Basic(optional = false)
    @Column(name = "value")
    private String value;

    @Basic(optional = true)
    @Column(name = "regulatory_text_citation")
    private String regulatoryTextCitation;

    @Basic(optional = true)
    @Column(name = "additional_information")
    private String additionalInformation;

    @Basic(optional = true)
    @Column(name = "start_day")
    private LocalDate startDay;

    @Basic(optional = true)
    @Column(name = "end_day")
    private LocalDate endDay;

    @Basic(optional = true)
    @Column(name = "required_day")
    private LocalDate requiredDay;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_type_id", insertable = true, updatable = true)
    private PracticeTypeEntity practiceType;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "functionalityTestedId")
    @Basic(optional = false)
    @Column(name = "functionality_tested_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<FunctionalityTestedCriteriaMapEntity> mappedCriteria = new HashSet<FunctionalityTestedCriteriaMapEntity>();

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private RuleEntity rule;


    public FunctionalityTested toDomain() {
        return FunctionalityTested.builder()
                .id(this.getId())
                .description(this.getName())
                .name(this.getNumber())
                .value(this.getValue())
                .regulatoryTextCitation(this.regulatoryTextCitation)
                .additionalInformation(additionalInformation)
                .startDay(this.startDay)
                .endDay(this.endDay)
                .requiredDay(this.requiredDay)
                .rule(this.rule != null ? this.rule.toDomain() : null)
                .practiceType(this.getPracticeType() != null ? this.getPracticeType().toDomain() : null)
                .build();
    }

    public FunctionalityTested toDomainWithCriteria() {
        return FunctionalityTested.builder()
                .id(this.getId())
                .description(this.getName())
                .name(this.getNumber())
                .value(this.getValue())
                .regulatoryTextCitation(this.regulatoryTextCitation)
                .additionalInformation(additionalInformation)
                .startDay(this.startDay)
                .endDay(this.endDay)
                .requiredDay(this.requiredDay)
                .rule(this.rule != null ? this.rule.toDomain() : null)
                .practiceType(this.getPracticeType() != null ? this.getPracticeType().toDomain() : null)
                .criteria(this.getMappedCriteria() != null ? this.getMappedCriteria().stream()
                        .map(mappedCriterion -> mappedCriterion.getCriterion().toDomain())
                        .collect(Collectors.toList()) : null)
                .build();
    }
}
