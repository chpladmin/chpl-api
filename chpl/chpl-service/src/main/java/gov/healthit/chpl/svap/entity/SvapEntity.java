package gov.healthit.chpl.svap.entity;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.WhereJoinTable;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.svap.domain.Svap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "svap")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SvapEntity {

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

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    public Svap toDomain() {
        return Svap.builder()
                .svapId(this.getSvapId())
                .approvedStandardVersion(this.getApprovedStandardVersion())
                .regulatoryTextCitation(this.getRegulatoryTextCitation())
                .replaced(this.getReplaced())
                .build();
    }

    public Svap toDomainWithCriteria() {
        return Svap.builder()
                .svapId(this.getSvapId())
                .approvedStandardVersion(this.getApprovedStandardVersion())
                .regulatoryTextCitation(this.getRegulatoryTextCitation())
                .replaced(this.getReplaced())
                .criteria(this.getCriteria().stream()
                        .map(crit -> crit.toDomain())
                        .collect(Collectors.toList()))
                .build();
    }

}
