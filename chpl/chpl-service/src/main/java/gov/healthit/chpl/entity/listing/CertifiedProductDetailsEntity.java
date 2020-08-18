package gov.healthit.chpl.entity.listing;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.util.Util;
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

    @Basic(optional = true)
    @Column(name = "meaningful_use_users")
    private Long meaningfulUseUsers;

    @Column(name = "meaningful_use_users_date")
    private Date meaningfulUseUsersDate;

    @Basic(optional = true)
    @Column(name = "sed_testing_end")
    private Date sedTestingEnd;

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

    @Column(name = "cures_update")
    private Boolean curesUpdate;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

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

    @Column(name = "transparency_attestation")
    @Type(type = "gov.healthit.chpl.entity.PostgresAttestationType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                    value = "gov.healthit.chpl.entity.AttestationType")
    })
    private AttestationType transparencyAttestation;

    @Column(name = "transparency_attestation_url")
    private String transparencyAttestationUrl;

    @Column(name = "rwt_eligibility_year")
    private Integer rwtEligibilityYear;

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

    public Date getCertificationStatusDate() {
        return Util.getNewDate(certificationStatusDate);
    }

    public void setCertificationStatusDate(Date certificationStatusDate) {
        this.certificationStatusDate = Util.getNewDate(certificationStatusDate);
    }

    public Date getDecertificationDate() {
        return Util.getNewDate(decertificationDate);
    }

    public void setDecertificationDate(Date decertificationDate) {
        this.decertificationDate = Util.getNewDate(decertificationDate);
    }

    public Date getCertificationDate() {
        return Util.getNewDate(certificationDate);
    }

    public void setCertificationDate(Date certificationDate) {
        this.certificationDate = Util.getNewDate(certificationDate);
    }

    public Date getDeveloperStatusDate() {
        return Util.getNewDate(developerStatusDate);
    }

    public void setDeveloperStatusDate(Date developerStatusDate) {
        this.developerStatusDate = Util.getNewDate(developerStatusDate);
    }

    public Date getMeaningfulUseUsersDate() {
        return Util.getNewDate(meaningfulUseUsersDate);
    }

    public void setMeaningfulUseUsersDate(Date meaningfulUseUsersDate) {
        this.meaningfulUseUsersDate = Util.getNewDate(meaningfulUseUsersDate);
    }
}
