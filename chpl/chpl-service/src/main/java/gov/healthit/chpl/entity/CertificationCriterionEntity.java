package gov.healthit.chpl.entity;

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

    /** Serial Version UID. */
    private static final long serialVersionUID = 5366674516357955978L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certification_criterion_id", nullable = false)
    @JoinColumn(name = "certification_criterion_id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @Column(name = "automated_measure_capable")
    private Boolean automatedMeasureCapable;

    @Basic(optional = true)
    @Column(name = "automated_numerator_capable")
    private Boolean automatedNumeratorCapable;

    @Basic(optional = false)
    @Column(name = "certification_edition_id", nullable = false)
    private Long certificationEditionId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_edition_id", unique = true, nullable = true, insertable = false,
            updatable = false)
    private CertificationEditionEntity certificationEdition;

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
    @Column(name = "requires_sed")
    private Boolean requiresSed;

    @Basic(optional = true)
    @Column(length = 250)
    private String title;

    @Basic(optional = true)
    @Column(name = "removed")
    private Boolean removed;

    public static CertificationCriterionEntity getNewCertificationCriterionEntity(CertificationCriterion criterion) {
        CertificationEditionEntity edition = new CertificationEditionEntity();
        edition.setId(criterion.getCertificationEditionId());

        return CertificationCriterionEntity.builder()
                .id(criterion.getId())
                .certificationEditionId(criterion.getCertificationEditionId())
                .certificationEdition(edition)
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
                .description(this.getDescription())
                .number(this.getNumber())
                .removed(this.getRemoved())
                .title(this.getTitle())
                .build();
    }

}
