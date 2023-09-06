package gov.healthit.chpl.optionalStandard.entity;

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

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "optional_standard_criteria_map")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OptionalStandardCriteriaMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "criterion_id")
    private Long certificationCriterionId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity criteria;

    @Column(name = "optional_standard_id")
    private Long optionalStandardId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "optional_standard_id", insertable = false, updatable = false)
    private OptionalStandardEntity optionalStandard;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public OptionalStandardCriteriaMap toDomain() {
        return OptionalStandardCriteriaMap.builder()
                .id(this.getId())
                .optionalStandard(this.getOptionalStandard().toDomain())
                .criterion(this.getCriteria().toDomain())
                .build();
    }
}
