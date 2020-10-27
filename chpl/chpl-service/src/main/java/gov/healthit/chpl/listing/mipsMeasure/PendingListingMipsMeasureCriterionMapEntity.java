package gov.healthit.chpl.listing.mipsMeasure;

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

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.Data;

@Entity
@Data
@Table(name = "pending_certified_product_mips_measure_criteria")
public class PendingListingMipsMeasureCriterionMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "pending_certified_product_mips_measure_id", nullable = false)
    private Long pendingListingMipsMeasureId;

    @Basic(optional = false)
    @Column(name = "certification_criterion_id", nullable = false)
    private Long certificationCriterionId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", unique = true, nullable = true, insertable = false, updatable = false)
    private CertificationCriterionEntity criterion;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private Date creationDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof PendingListingMipsMeasureCriterionMapEntity)) {
            return false;
        }
        PendingListingMipsMeasureCriterionMapEntity otherEntity = (PendingListingMipsMeasureCriterionMapEntity) obj;
        if (this.certificationCriterionId == null && otherEntity.certificationCriterionId != null) {
            return false;
        } else if (this.certificationCriterionId != null && otherEntity.certificationCriterionId == null) {
            return false;
        }
        return ObjectUtils.allNotNull(this.certificationCriterionId, otherEntity.certificationCriterionId)
                && this.certificationCriterionId.equals(otherEntity.certificationCriterionId);
    }

    @Override
    public int hashCode() {
        if (this.certificationCriterionId == null) {
            return -1;
        }
        return this.certificationCriterionId.hashCode();
    }
}
