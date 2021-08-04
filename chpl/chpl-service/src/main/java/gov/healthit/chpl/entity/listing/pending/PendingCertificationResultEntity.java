package gov.healthit.chpl.entity.listing.pending;

import java.util.Date;
import java.util.HashSet;
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

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.Data;

@Data
@Entity
@Table(name = "pending_certification_result")
public class PendingCertificationResultEntity {

    @Transient
    private Boolean hasAdditionalSoftware;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", unique = true, nullable = true)
    private CertificationCriterionEntity mappedCriterion;

    @Basic(optional = false)
    @Column(name = "pending_certified_product_id", nullable = false)
    private Long pendingCertifiedProductId;

    @Column(name = "meets_criteria")
    private Boolean meetsCriteria;

    @Column(name = "gap")
    private Boolean gap;

    @Column(name = "sed")
    private Boolean sed;

    @Column(name = "g1_success")
    private Boolean g1Success;

    @Column(name = "g2_success")
    private Boolean g2Success;

    @Column(name = "attestation_answer")
    private Boolean attestationAnswer;

    @Column(name = "api_documentation")
    private String apiDocumentation;

    @Column(name = "export_documentation")
    private String exportDocumentation;

    @Column(name = "documentation_url")
    private String documentationUrl;

    @Column(name = "use_cases")
    private String useCases;

    @Column(name = "service_base_url_list")
    private String serviceBaseUrlList;

    @Column(name = "privacy_security_framework")
    private String privacySecurityFramework;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertificationResultId")
    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Set<PendingCertificationResultUcdProcessEntity> ucdProcesses;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertificationResultId")
    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Set<PendingCertificationResultOptionalStandardEntity> optionalStandards;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertificationResultId")
    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Set<PendingCertificationResultTestStandardEntity> testStandards;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertificationResultId")
    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Set<PendingCertificationResultTestFunctionalityEntity> testFunctionality;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertificationResultId")
    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Set<PendingCertificationResultAdditionalSoftwareEntity> additionalSoftware;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertificationResultId")
    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Set<PendingCertificationResultTestProcedureEntity> testProcedures;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertificationResultId")
    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Set<PendingCertificationResultTestDataEntity> testData;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertificationResultId")
    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Set<PendingCertificationResultTestToolEntity> testTools;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCertificationResultId")
    @Basic(optional = false)
    @Column(name = "pending_certification_result_id", nullable = false)
    private Set<PendingCertificationResultTestTaskEntity> testTasks;

    public PendingCertificationResultEntity() {
        ucdProcesses = new HashSet<PendingCertificationResultUcdProcessEntity>();
        optionalStandards = new HashSet<PendingCertificationResultOptionalStandardEntity>();
        testStandards = new HashSet<PendingCertificationResultTestStandardEntity>();
        testFunctionality = new HashSet<PendingCertificationResultTestFunctionalityEntity>();
        additionalSoftware = new HashSet<PendingCertificationResultAdditionalSoftwareEntity>();
        testProcedures = new HashSet<PendingCertificationResultTestProcedureEntity>();
        testData = new HashSet<PendingCertificationResultTestDataEntity>();
        testTools = new HashSet<PendingCertificationResultTestToolEntity>();
        testTasks = new HashSet<PendingCertificationResultTestTaskEntity>();
    }
}
