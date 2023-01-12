package gov.healthit.chpl.functionalityTested;

import java.io.Serializable;
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

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@Table(name = "functionality_tested_criteria_map")
public class FunctionalityTestedCriteriaMapEntity implements Serializable {
    private static final long serialVersionUID = 6446486138564063907L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "criteria_id")
    private Long certificationCriterionId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", insertable = false, updatable = false)
    private CertificationCriterionEntity criterion;

    @Column(name = "functionality_tested_id")
    private Long functionalityTestedId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "functionality_tested_id", insertable = false, updatable = false)
    private FunctionalityTestedEntity functionalityTested;

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

    public FunctionalityTestedCriteriaMap toDomain() {
        return FunctionalityTestedCriteriaMap.builder()
                .id(this.getId())
                .creationDate(this.getCreationDate())
                .criterion(this.getCriterion() != null ? this.getCriterion().toDomain() : null)
                .deleted(this.getDeleted())
                .functionalityTested(this.getFunctionalityTested() != null ? this.getFunctionalityTested().toDomain() : null)
                .lastModifiedDate(this.getLastModifiedDate())
                .lastModifiedUser(this.getLastModifiedUser())
                .build();
    }
}
