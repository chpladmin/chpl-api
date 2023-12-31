package gov.healthit.chpl.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.CQMCriterion;
import gov.healthit.chpl.util.NullSafeEvaluator;
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
@Table(name = "cqm_criterion", schema = "openchpl")
public class CQMCriterionEntity extends EntityAudit {
    private static final long serialVersionUID = -6056370503196234368L;

    @Basic(optional = true)
    @Column(name = "cms_id", length = 15)
    private String cmsId;

    @Column(name = "cqm_criterion_type_id", nullable = false)
    private Long cqmCriterionTypeId;

    @Basic(optional = true)
    @Column(name = "cqm_domain", length = 250)
    private String cqmDomain;

    @Basic(optional = true)
    @Column(name = "cqm_version_id", length = 10)
    private Long cqmVersionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cqm_version_id", insertable = false, updatable = false)
    private CQMVersionEntity cqmVersion;

    @Basic(optional = true)
    @Column(length = 1000)
    private String description;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "cqm_criterion_id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @Column(name = "nqf_number", length = 50)
    private String nqfNumber;

    @Basic(optional = true)
    @Column(length = 20)
    private String number;

    @Basic(optional = true)
    @Column(length = 250)
    private String title;

    @Basic(optional = false)
    @Column(name = "retired", length = 10)
    private Boolean retired;

    public CQMCriterion toDomain() {
        return CQMCriterion.builder()
                .criterionId(this.getId())
                .cmsId(this.getCmsId())
                .cqmCriterionTypeId(this.getCqmCriterionTypeId())
                .cqmDomain(this.getCqmDomain())
                .cqmVersionId(this.getCqmVersionId())
                .cqmVersion(NullSafeEvaluator.eval(() -> this.getCqmVersion().getVersion(), ""))
                .description(this.getDescription())
                .nqfNumber(this.getNqfNumber())
                .number(this.getNumber())
                .title(this.getTitle())
                .build();
    }
}
