package gov.healthit.chpl.entity.search;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import gov.healthit.chpl.util.Util;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@Entity
@Table(name = "certified_product_search")
public class CertifiedProductBasicSearchResultEntity {
    private static final long serialVersionUID = -2928065796550377869L;

    @Id
    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

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

    @Column(name = "vendor_id")
    private Long developerId;

    @Column(name = "vendor_name")
    private String developer;

    @Column(name = "vendor_status_name")
    private String developerStatus;

    @Column(name = "owner_history")
    private String previousDevelopers;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "certification_status_name")
    private String certificationStatus;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @Column(name = "decertification_date")
    private Date decertificationDate;

    @Column(name = "transparency_attestation_url")
    private String transparencyAttestationUrl;

    @Column(name = "api_documentation")
    private String apiDocumentation;

    @Column(name = "service_base_url_list")
    private String serviceBaseUrlList;

    @Column(name = "surveillance_count")
    private Long surveillanceCount;

    @Column(name = "open_surveillance_count")
    private Long openSurveillanceCount;

    @Column(name = "closed_surveillance_count")
    private Long closedSurveillanceCount;

    @Column(name = "open_nonconformity_count")
    private Long openSurveillanceNonConformityCount;

    @Column(name = "closed_nonconformity_count")
    private Long closedSurveillanceNonConformityCount;

    @Column(name = "surv_dates")
    private String surveillanceDates;

    @Column(name = "status_events")
    private String statusEvents;

    @Column(name = "meaningful_use_users")
    private Long meaningfulUseUserCount;

    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    @Column(name = "meaningful_use_users_date")
    private Date meaningfulUseUserDate;

    @Column(name = "certs")
    private String certs; // comma-separated list of all certification criteria
    // met by the certified product

    @Column(name = "cqms")
    private String cqms; // comma-separated list of all cqms met by the
    // certified product

    @Column(name = "parent")
    private String parent; // comma-separated list of all parents

    @Column(name = "child")
    private String child; // comma-separated list of all children

    public Date getMeaningfulUseUserDate() {
        return Util.getNewDate(meaningfulUseUserDate);
    }

    public void setMeaningfulUseUserDate(Date meaningfulUseUserDate) {
        this.meaningfulUseUserDate = Util.getNewDate(meaningfulUseUserDate);
    }

    public Date getCertificationDate() {
        return Util.getNewDate(certificationDate);
    }

    public void setCertificationDate(Date certificationDate) {
        this.certificationDate = Util.getNewDate(certificationDate);
    }

    public Date getDecertificationDate() {
        return Util.getNewDate(decertificationDate);
    }

    public void setDecertificationDate(Date decertificationDate) {
        this.decertificationDate = Util.getNewDate(decertificationDate);
    }
}
