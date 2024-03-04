package gov.healthit.chpl.surveillance.report.entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.surveillance.report.domain.RelevantListing;
import lombok.Data;
import lombok.Singular;

@Data
@Entity
@Immutable
@Table(name = "certified_product_details")
public class ListingWithPrivilegedSurveillanceEntity {
    @Id
    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "certification_status_id")
    private Long certificationStatusId;

    @Column(name = "certification_status_name")
    private String certificationStatusName;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @Column(name = "year")
    private String year;

    @Column(name = "cures_update")
    private Boolean curesUpdate;

    @Column(name = "vendor_id")
    private Long developerId;

    @Column(name = "vendor_name")
    private String developerName;

    @Column(name = "vendor_code")
    private String developerCode;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_version_id")
    private Long productVersionId;

    @Column(name = "product_version")
    private String productVersion;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Column(name = "certification_body_name")
    private String certificationBodyName;

    @Column(name = "certification_body_code")
    private String certificationBodyCode;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "deleted")
    private Boolean deleted;

    @Singular
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certifiedProductId")
    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<PrivilegedSurveillanceEntity> surveillances = new HashSet<PrivilegedSurveillanceEntity>();

    public RelevantListing toDomain() {
        RelevantListing listing = new RelevantListing();
        listing.setAcb(CertificationBody.builder()
                .id(this.getCertificationBodyId())
                .name(this.getCertificationBodyName())
                .acbCode(this.getCertificationBodyCode())
                .build());
        listing.setCertificationDate(this.getCertificationDate().getTime());
        listing.setCertificationStatus(this.getCertificationStatusName());
        listing.setChplProductNumber(this.getChplProductNumber());
        listing.setCuresUpdate(this.getCuresUpdate());
        listing.setEdition(this.getYear());
        listing.setId(this.getId());
        //TODO get privileged surv
        return listing;
    }
}
