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

    @Deprecated
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
    private LocalDate sedTestingEnd;

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

    @Column(name = "mandatory_disclosures")
    private String mandatoryDisclosures;

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

    @Column(name = "promoting_interoperability_user_count")
    private Long promotingInteroperabilityUserCount;

    @Deprecated
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
    private Set<CertificationResultDetailsEntity> certificationResults = new HashSet<CertificationResultDetailsEntity>();

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
}
