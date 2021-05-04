package gov.healthit.chpl.entity.surveillance;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import gov.healthit.chpl.domain.surveillance.SurveillanceBasic;
import lombok.Data;

@Entity
@Data
@Table(name = "surveillance_basic")
public class SurveillanceBasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "friendly_id", insertable = false, updatable = false)
    private String friendlyId;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "type_id")
    private Long surveillanceTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private SurveillanceTypeEntity surveillanceType;

    @Column(name = "randomized_sites_used")
    private Integer numRandomizedSites;

    @Column(name = "open_nonconformity_count")
    private Integer numOpenNonconformities;

    @Column(name = "closed_nonconformity_count")
    private Integer numClosedNonconformities;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Column(name = "user_permission_id")
    private Long userPermissionId;

    @Transient
    private String chplProductNumber;

    public SurveillanceBasic buildSurveillanceBasic() {
        return SurveillanceBasic.builder()
                .certifiedProductId(this.getCertifiedProductId())
                .chplProductNumber(this.getChplProductNumber())
                .endDate(this.getEndDate())
                .friendlyId(this.getFriendlyId())
                .id(this.getId())
                .numClosedNonconformities(this.getNumClosedNonconformities())
                .numOpenNonconformities(this.getNumOpenNonconformities())
                .numRandomizedSites(this.getNumRandomizedSites())
                .startDate(this.getStartDate())
                .surveillanceType(this.getSurveillanceType() != null ? this.getSurveillanceType().buildSurveillanceType() : null)
                .surveillanceTypeId(this.getSurveillanceTypeId())
                .userPermissionId(this.getUserPermissionId())
            .build();
    }
}
