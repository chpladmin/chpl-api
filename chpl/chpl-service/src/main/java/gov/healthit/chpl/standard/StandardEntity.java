package gov.healthit.chpl.standard;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
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
public class StandardEntity implements Serializable {
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

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public Standard toDomain() {
        return Standard.builder()
                .id(this.getId())
                .value(this.getValue())
                .regulatoryTextCitation(this.regulatoryTextCitation)
                .additionalInformation(additionalInformation)
                .startDay(this.startDay)
                .endDay(this.endDay)
                .requiredDay(this.requiredDay)
                .rule(this.rule != null ? this.rule.toDomain() : null)
                .build();
    }

    public Standard toDomainWithCriteria() {
        return Standard.builder()
                .id(this.getId())
                .value(this.getValue())
                .regulatoryTextCitation(this.regulatoryTextCitation)
                .additionalInformation(additionalInformation)
                .startDay(this.startDay)
                .endDay(this.endDay)
                .requiredDay(this.requiredDay)
                .rule(this.rule != null ? this.rule.toDomain() : null)
                .criteria(this.getMappedCriteria() != null ? this.getMappedCriteria().stream()
                        .map(mappedCriterion -> mappedCriterion.getCriterion().toDomain())
                        .collect(Collectors.toList()) : null)
                .build();
    }

}
