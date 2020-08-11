package gov.healthit.chpl.entity.listing;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certified_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CertifiedProductEntity implements Serializable {
    private static final long serialVersionUID = -2437147151682759808L;

    private static final int OTHER_ACB_LENGTH = 64;
    private static final int CHPL_ID_LENGTH = 250;
    private static final int REPORT_FILE_LOCATION_LENGTH = 255;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long id;

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

    @Basic(optional = true)
    @Column(name = "acb_certification_id", length = CHPL_ID_LENGTH)
    private String acbCertificationId;

    @Basic(optional = false)
    @Column(name = "certification_body_id", nullable = false)
    private Long certificationBodyId;

    @Basic(optional = false)
    @Column(name = "certification_edition_id", nullable = false)
    private Long certificationEditionId;

    @Basic(optional = true)
    @Column(name = "chpl_product_number", length = CHPL_ID_LENGTH)
    private String chplProductNumber;

    @Basic(optional = true)
    @Column(name = "practice_type_id", nullable = true)
    private Long practiceTypeId;

    @Basic(optional = true)
    @Column(name = "product_classification_type_id", nullable = true)
    private Long productClassificationTypeId;

    @Basic(optional = false)
    @Column(name = "product_version_id", nullable = false)
    private Long productVersionId;

    @Basic(optional = true)
    @Column(name = "report_file_location", length = REPORT_FILE_LOCATION_LENGTH)
    private String reportFileLocation;

    @Basic(optional = true)
    @Column(name = "sed_report_file_location")
    private String sedReportFileLocation;

    @Basic(optional = true)
    @Column(name = "sed_intended_user_description")
    private String sedIntendedUserDescription;

    @Basic(optional = true)
    @Column(name = "sed_testing_end")
    private Date sedTestingEnd;

    @Basic(optional = true)
    @Column(name = "other_acb", length = OTHER_ACB_LENGTH)
    private String otherAcb;

    @Column(name = "transparency_attestation_url")
    private String transparencyAttestationUrl;

    @Column(name = "ics")
    private Boolean ics;

    @Column(name = "sed")
    private Boolean sedTesting;

    @Column(name = "qms")
    private Boolean qmsTesting;

    @Column(name = "accessibility_certified")
    private Boolean accessibilityCertified;

    @Column(name = "product_additional_software")
    private String productAdditionalSoftware;

    @Column(name = "pending_certified_product_id")
    private Long pendingCertifiedProductId;

    @Basic(optional = true)
    @OneToMany(targetEntity = CertificationResultEntity.class, mappedBy = "certifiedProduct", fetch = FetchType.LAZY)
    private List<CertificationResultEntity> certificationResult;

    @Basic(optional = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certified_product_id", nullable = false, insertable = false, updatable = false)
    private CertifiedProductEntity certifiedProduct;

    @Column(name = "rwt_eligibility_year")
    private Integer rwtEligibilityYear;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(nullable = false, updatable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = true)
    @Column(name = "rwt_plan_url", nullable = true)
    private String rwtPlanUrl;

    @Basic(optional = true)
    @Column(name = "rwt_plan_submission_date", nullable = true)
    private Date rwtPlanSubmissionDate;

    @Basic(optional = true)
    @Column(name = "rwt_results_url", nullable = true)
    private String rwtResultsUrl;

    @Basic(optional = true)
    @Column(name = "rwt_results_submission_date", nullable = true)
    private Date rwtResultsSubmissionDate;


    /**
     * Constructor taking a given ID.
     *
     * @param id
     *            to set
     */
    public CertifiedProductEntity(Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return CertifiedProductEntity.class;
    }

}
