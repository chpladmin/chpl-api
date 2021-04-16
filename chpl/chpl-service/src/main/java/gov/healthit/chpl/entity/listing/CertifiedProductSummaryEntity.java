package gov.healthit.chpl.entity.listing;

import java.io.Serializable;
import java.time.LocalDate;
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

import gov.healthit.chpl.certifiedproduct.entity.CertificationResultDetailsEntityv2;
import gov.healthit.chpl.util.Util;
import lombok.Data;

@Entity
@Immutable
@Table(name = "certified_product_summary")
@Data
public class CertifiedProductSummaryEntity implements Serializable {
    private static final long serialVersionUID = -7006206379019745873L;

    @Id
    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @Column(name = "product_version_id")
    private Long productVersionId;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "report_file_location")
    private String reportFileLocation;

    @Column(name = "sed_report_file_location")
    private String sedReportFileLocation;

    @Column(name = "sed_intended_user_description")
    private String sedIntendedUserDescription;

    @Column(name = "sed_testing_end")
    private Date sedTestingEnd;

    @Column(name = "acb_certification_id")
    private String acbCertificationId;

    @Column(name = "practice_type_id")
    private Long practiceTypeId;

    @Column(name = "product_classification_type_id")
    private Long productClassificationTypeId;

    @Column(name = "product_additional_software")
    private String productAdditionalSoftware;

    @Column(name = "other_acb")
    private String otherAcb;

    @Column(name = "transparency_attestation_url")
    private String transparencyAttestationUrl;

    @Column(name = "ics")
    private Boolean ics;

    @Column(name = "sed")
    private Boolean sed;

    @Column(name = "qms")
    private Boolean qms;

    @Column(name = "accessibility_certified")
    private Boolean accessibilityCertified;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "version_code")
    private String versionCode;

    @Column(name = "ics_code")
    private String icsCode;

    @Column(name = "additional_software_code")
    private String additionalSoftwareCode;

    @Column(name = "certified_date_code")
    private String certifiedDateCode;

    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "last_modified_user")
    private String lastModifiedUser;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "meaningful_use_users")
    private Long meaninigfulUseUsers;

    @Column(name = "pending_certified_product_id")
    private Long pendingCertifiedProductId;

    @Column(name = "year")
    private String year;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "vendor_name")
    private String developerName;

    @Column(name = "vendor_code")
    private String developerCode;

    @Column(name = "certification_status")
    private String certificationStatus;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "cures_update")
    private Boolean curesUpdate;

    @Column(name = "acb_code")
    private String acbCode;

    @Column(name = "certification_body_name")
    private String certificationBodyName;

    @Column(name = "certification_body_website")
    private String certificationBodyWebsite;

    @Column(name = "version")
    private String version;

    @Column(name = "full_name")
    private String developerContactName;

    @Column(name = "email")
    private String developerContactEmail;

    @Column(name = "phone_number")
    private String developerContactPhone;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certifiedProductId")
    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<CertificationResultDetailsEntityv2> certificationResults = new HashSet<CertificationResultDetailsEntityv2>();

    @Column(name = "rwt_eligibility_year")
    private Integer rwtEligibilityYear;

    @Column(name = "rwt_plans_url")
    private String rwtPlansUrl;

    @Column(name = "rwt_plans_check_date")
    private LocalDate rwtPlansCheckDate;

    @Column(name = "rwt_results_url")
    private String rwtResultsUrl;

    @Column(name = "rwt_results_check_date")
    private LocalDate rwtResultsCheckDate;

    @Column(name = "svap_notice_url")
    private String svapNoticeUrl;

    public Date getSedTestingEnd() {
        return Util.getNewDate(sedTestingEnd);
    }

    public void setSedTestingEnd(Date sedTestingEnd) {
        this.sedTestingEnd = Util.getNewDate(sedTestingEnd);
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
}
