package gov.healthit.chpl.entity.search;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.util.Util;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents one row of one listing's search result data.
 * Will need to be combined with multiple other rows to make a complete listing.
 * @author kekey
 *
 */
@Data
@NoArgsConstructor
@Entity
@Immutable
public class CertifiedProductListingSearchResultEntity {
    private static final long serialVersionUID = -2928445796550377509L;

    @Id
    @Column(name = "unique_id")
    private int uniqueId;

    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "certification_status_name")
    private String certificationStatus;

    @Column(name = "meaningful_use_users")
    private Long meaningfulUseUserCount;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @Column(name = "meaningful_use_users_date")
    private Date meaningfulUseUsersDate;

    @Column(name = "transparency_attestation_url")
    private String transparencyAttestationUrl;

    @Column(name = "year")
    private String edition;

    @Column(name = "cures_update")
    private Boolean curesUpdate;

    @Column(name = "certification_body_name")
    private String acbName;

    @Column(name = "acb_certification_id")
    private String acbCertificationId;

    @Column(name = "practice_type_name")
    private String practiceTypeName;

    @Column(name = "product_version")
    private String version;

    @Column(name = "product_name")
    private String product;

    @Column(name = "vendor_name")
    private String developer;

    @Column(name = "prev_vendor")
    private String previousDeveloperOwner;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @Column(name = "certification_date")
    private Date certificationDate;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @Column(name = "decertification_date")
    private Date decertificationDate;

    @Column(name = "count_surveillance_activities")
    private Integer countSurveillance;

    @Column(name = "count_open_nonconformities")
    private Integer countOpenSurveillanceNonconformities;

    @Column(name = "count_closed_nonconformities")
    private Integer countClosedSurveillanceNonconformities;

    @Column(name = "cert_number")
    private String cert;

    @Column(name = "cqm_number")
    private String cqm;

    public Date getCertificationDate() {
        return Util.getNewDate(certificationDate);
    }

    public void setCertificationDate(final Date certificationDate) {
        this.certificationDate = Util.getNewDate(certificationDate);
    }

    public Date getDecertificationDate() {
        return Util.getNewDate(decertificationDate);
    }

    public void setDecertificationDate(final Date decertificationDate) {
        this.decertificationDate = Util.getNewDate(decertificationDate);
    }

    public Date getMeaningfulUseUsersDate() {
        return Util.getNewDate(meaningfulUseUsersDate);
    }

    public void setMeaningfulUseUsersDate(Date meaningfulUseUsersDate) {
        this.meaningfulUseUsersDate = Util.getNewDate(meaningfulUseUsersDate);
    }
}
