package gov.healthit.chpl.entity.listing;

import java.io.Serializable;
import java.time.LocalDate;
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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "certified_product")
@Data
@Builder
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
    private LocalDate sedTestingEnd;

    @Basic(optional = true)
    @Column(name = "other_acb", length = OTHER_ACB_LENGTH)
    private String otherAcb;

    @Column(name = "mandatory_disclosures")
    private String mandatoryDisclosures;

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

    @Basic(optional = true)
    @OneToMany(targetEntity = CertificationResultEntity.class, mappedBy = "certifiedProduct", fetch = FetchType.LAZY)
    private List<CertificationResultEntity> certificationResult;

    @Basic(optional = true)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "certified_product_id", nullable = false, insertable = false, updatable = false)
    private CertifiedProductEntity certifiedProduct;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "deleted", insertable = false, updatable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = true)
    @Column(name = "rwt_plans_url", nullable = true)
    private String rwtPlansUrl;

    @Basic(optional = true)
    @Column(name = "rwt_plans_check_date", nullable = true)
    private LocalDate rwtPlansCheckDate;

    @Basic(optional = true)
    @Column(name = "rwt_results_url", nullable = true)
    private String rwtResultsUrl;

    @Basic(optional = true)
    @Column(name = "rwt_results_check_date", nullable = true)
    private LocalDate rwtResultsCheckDate;

    @Basic(optional = true)
    @Column(name = "svap_notice_url", nullable = true)
    private String svapNoticeUrl;

    public CertifiedProductEntity(Long id) {
        this.id = id;
    }
}
