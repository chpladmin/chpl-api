package gov.healthit.chpl.entity.listing;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.entity.ProductEntity;
import lombok.Data;

@Entity
@Table(name = "certified_product_details")
@Data
public class CertifiedProductDetailsEntity {

    /** Serial Version UID. */
    private static final long serialVersionUID = -2928065796550377879L;

    @Id
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

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "report_file_location")
    private String reportFileLocation;

    @Column(name = "sed_report_file_location")
    private String sedReportFileLocation;

    @Basic(optional = true)
    @Column(name = "sed_intended_user_description")
    private String sedIntendedUserDescription;

    @Column(name = "promoting_interoperability_user_count")
    private Long promotingInteroperabilityUserCount;

    @Column(name = "promoting_interoperability_user_count_date")
    private LocalDate promotingInteroperabilityUserCountDate;

    @Basic(optional = true)
    @Column(name = "sed_testing_end")
    private LocalDate sedTestingEnd;

    @Column(name = "acb_certification_id")
    private String acbCertificationId;

    @Column(name = "practice_type_id")
    private Long practiceTypeId;

    @Column(name = "practice_type_name")
    private String practiceTypeName;

    @Column(name = "product_classification_type_id")
    private Long productClassificationTypeId;

    @Column(name = "other_acb")
    private String otherAcb;

    @Column(name = "certification_status_id")
    private Long certificationStatusId;

    @Column(name = "certification_status_name")
    private String certificationStatusName;

    @Column(name = "last_certification_status_change")
    private Date certificationStatusDate;

    @Deprecated
    @Column(name = "cures_update")
    private Boolean curesUpdate;

    @Deprecated
    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @Deprecated
    @Column(name = "year")
    private String year;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Column(name = "certification_body_name")
    private String certificationBodyName;

    @Column(name = "certification_body_code")
    private String certificationBodyCode;

    @Column(name = "acb_is_retired")
    private Boolean acbIsRetired;

    @Column(name = "product_classification_name")
    private String productClassificationName;

    @Column(name = "product_version_id")
    private Long productVersionId;

    @Column(name = "product_version")
    private String productVersion;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", unique = true, nullable = true, insertable = false, updatable = false)
    private ProductEntity product;

    @Column(name = "vendor_id")
    private Long developerId;

    @Column(name = "vendor_name")
    private String developerName;

    @Column(name = "vendor_code")
    private String developerCode;

    @Column(name = "vendor_website")
    private String developerWebsite;

    @Column(name = "vendor_status_id")
    private Long developerStatusId;

    @Column(name = "vendor_status_name")
    private String developerStatusName;

    @Column(name = "last_vendor_status_change")
    private Date developerStatusDate;

    @Column(name = "self_developer")
    private Boolean selfDeveloper;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "street_line_1")
    private String streetLine1;

    @Column(name = "street_line_2")
    private String streetLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "country")
    private String country;

    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "title")
    private String title;

    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "decertification_date")
    private Date decertificationDate;

    @Column(name = "count_certifications")
    private Integer countCertifications;

    @Column(name = "count_cqms")
    private Integer countCqms;

    @Column(name = "count_surveillance_activities")
    private Integer countSurveillance;

    @Column(name = "count_open_surveillance_activities")
    private Integer countOpenSurveillance;

    @Column(name = "count_closed_surveillance_activities")
    private Integer countClosedSurveillance;

    @Column(name = "count_open_nonconformities")
    private Integer countOpenNonconformities;

    @Column(name = "count_closed_nonconformities")
    private Integer countClosedNonconformities;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "deleted")
    private Boolean deleted;

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

    @Column(name = "mandatory_disclosures")
    private String mandatoryDisclosures;

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

    public CertifiedProduct toCertifiedProduct() {
        return CertifiedProduct.builder()
                .id(this.getId())
                .certificationDate(this.getCertificationDate().getTime())
                .certificationStatus(this.getCertificationStatusName())
                .chplProductNumber(this.getChplProductNumber())
                .curesUpdate(this.getCuresUpdate())
                .edition(this.getYear())
                .lastModifiedDate(this.getLastModifiedDate().getTime())
                .build();
    }
}

