package gov.healthit.chpl.standard;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.criteriaattribute.rule.RuleEntity;
import gov.healthit.chpl.entity.EntityAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "standard")
@Data
public class StandardEntity extends EntityAudit implements Serializable {
    private static final long serialVersionUID = 8610348267368158172L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

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
    @Column(name = "group_name")
    private String groupName;

    @Basic(optional = true)
    @Column(name = "start_day")
    private LocalDate startDay;

    @Basic(optional = true)
    @Column(name = "end_day")
    private LocalDate endDay;

    @Basic(optional = true)
    @Column(name = "required_day")
    private LocalDate requiredDay;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "standardId")
    @Basic(optional = false)
    @Column(name = "standard_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<StandardCriteriaMapEntity> mappedCriteria = new HashSet<StandardCriteriaMapEntity>();

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_id")
    private RuleEntity rule;

    public Standard toDomain() {
        return Standard.builder()
                .id(this.getId())
                .value(this.getValue())
                .regulatoryTextCitation(this.regulatoryTextCitation)
                .additionalInformation(additionalInformation)
                .groupName(this.groupName)
                .startDay(this.startDay)
                .endDay(this.endDay)
                .requiredDay(this.requiredDay)
                .rule(this.rule != null ? this.rule.toDomain() : null)
                .build();
    }

    public Standard toDomainWithCriteria(CertificationCriterionComparator criterionComparator) {
        return Standard.builder()
                .id(this.getId())
                .value(this.getValue())
                .regulatoryTextCitation(this.regulatoryTextCitation)
                .additionalInformation(additionalInformation)
                .groupName(this.groupName)
                .startDay(this.startDay)
                .endDay(this.endDay)
                .requiredDay(this.requiredDay)
                .rule(this.rule != null ? this.rule.toDomain() : null)
                .criteria(this.getMappedCriteria() != null ? this.getMappedCriteria().stream()
                        .map(mappedCriterion -> mappedCriterion.getCriterion().toDomain())
                        .sorted(criterionComparator)
                        .collect(Collectors.toCollection(ArrayList::new)) : null)
                .build();
    }

}
