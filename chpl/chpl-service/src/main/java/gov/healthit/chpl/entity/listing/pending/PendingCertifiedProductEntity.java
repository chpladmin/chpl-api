package gov.healthit.chpl.entity.listing.pending;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import gov.healthit.chpl.entity.AddressEntity;
import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.listing.mipsMeasure.PendingListingMipsMeasureEntity;
import lombok.Data;

@Entity
@Data
@Table(name = "pending_certified_product")
public class PendingCertifiedProductEntity {

    @Transient
    private List<String> errorMessages = new ArrayList<String>();

    /**
     * fields we generate mostly from spreadsheet values
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "pending_certified_product_id", nullable = false)
    private Long id;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "warning_count")
    private Integer warningCount;

    @Column(name = "practice_type_id")
    private Long practiceTypeId;

    @Column(name = "vendor_id")
    private Long developerId;

    @Column(name = "vendor_contact_id")
    private Long developerContactId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_address_id", unique = true, nullable = true)
    private AddressEntity developerAddress;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_version_id")
    private Long productVersionId;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Column(name = "product_classification_id")
    private Long productClassificationId;

    /**
     * fields directly from the spreadsheet
     **/
    @Column(name = "unique_id")
    private String uniqueId;

    @Column(name = "record_status")
    private String recordStatus;

    @Column(name = "practice_type")
    private String practiceType;

    @Column(name = "vendor_name")
    private String developerName;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_version")
    private String productVersion;

    @Column(name = "certification_edition")
    private String certificationEdition;

    @Column(name = "acb_certification_id")
    private String acbCertificationId;

    @Column(name = "certification_body_name")
    private String certificationBodyName;

    @Column(name = "product_classification_name")
    private String productClassificationName;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "vendor_street_address")
    private String developerStreetAddress;

    @Column(name = "vendor_transparency_attestation")
    @Type(type = "gov.healthit.chpl.entity.PostgresAttestationType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                    value = "gov.healthit.chpl.entity.AttestationType")
    })
    private AttestationType transparencyAttestation;

    @Column(name = "vendor_transparency_attestation_url")
    private String transparencyAttestationUrl;

    @Column(name = "vendor_city")
    private String developerCity;

    @Column(name = "vendor_state")
    private String developerState;

    @Column(name = "vendor_zip_code")
    private String developerZipCode;

    @Column(name = "vendor_website")
    private String developerWebsite;

    @Column(name = "vendor_email")
    private String developerEmail;

    @Column(name = "vendor_contact_name")
    private String developerContactName;

    @Column(name = "vendor_phone")
    private String developerPhoneNumber;

    @Column(name = "self_developer")
    private Boolean selfDeveloper;

    @Column(name = "test_report_url")
    private String reportFileLocation;

    @Column(name = "sed_report_file_location")
    private String sedReportFileLocation;

    @Column(name = "sed_intended_user_description")
    private String sedIntendedUserDescription;

    @Column(name = "sed_testing_end")
    private Date sedTestingEnd;

    @Column(name = "ics")
    private Boolean ics;

    @Column(name = "accessibility_certified")
    private Boolean accessibilityCertified;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertifiedProductId")
    @Basic(optional = false)
    @Column(name = "pending_certified_product_id", nullable = false)
    private Set<PendingCertificationResultEntity> certificationCriterion;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertifiedProductId")
    @Basic(optional = false)
    @Column(name = "pending_certified_product_id", nullable = false)
    private Set<PendingCqmCriterionEntity> cqmCriterion;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertifiedProductId")
    @Basic(optional = false)
    @Column(name = "pending_certified_product_id", nullable = false)
    private Set<PendingCertifiedProductQmsStandardEntity> qmsStandards;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertifiedProductId")
    @Basic(optional = false)
    @Column(name = "pending_certified_product_id", nullable = false)
    private Set<PendingListingMipsMeasureEntity> mipsMeasures;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertifiedProductId")
    @Basic(optional = false)
    @Column(name = "pending_certified_product_id", nullable = false)
    private Set<PendingCertifiedProductTestingLabMapEntity> testingLabs;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertifiedProductId")
    @Basic(optional = false)
    @Column(name = "pending_certified_product_id", nullable = false)
    private Set<PendingCertifiedProductTargetedUserEntity> targetedUsers;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertifiedProductId")
    @Basic(optional = false)
    @Column(name = "pending_certified_product_id", nullable = false)
    private Set<PendingCertifiedProductAccessibilityStandardEntity> accessibilityStandards;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertifiedProductId")
    @Basic(optional = false)
    @Column(name = "pending_certified_product_id", nullable = false)
    private Set<PendingCertifiedProductParentListingEntity> parentListings;

    @Basic(optional = false)
    @Column(name = "has_qms", nullable = false)
    private Boolean hasQms;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    /**
     * Default constructor.
     */
    public PendingCertifiedProductEntity() {
        testingLabs = new HashSet<PendingCertifiedProductTestingLabMapEntity>();
        certificationCriterion = new HashSet<PendingCertificationResultEntity>();
        cqmCriterion = new HashSet<PendingCqmCriterionEntity>();
        qmsStandards = new HashSet<PendingCertifiedProductQmsStandardEntity>();
        //mipsMeasures = new HashSet<PendingListingMipsMeasureEntity>();
        targetedUsers = new HashSet<PendingCertifiedProductTargetedUserEntity>();
        accessibilityStandards = new HashSet<PendingCertifiedProductAccessibilityStandardEntity>();
        parentListings = new HashSet<PendingCertifiedProductParentListingEntity>();
    }
}
