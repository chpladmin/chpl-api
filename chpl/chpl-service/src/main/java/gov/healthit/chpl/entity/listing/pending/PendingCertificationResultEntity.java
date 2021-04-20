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
import gov.healthit.chpl.util.Util;

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
        testStandards = new HashSet<PendingCertificationResultTestStandardEntity>();
        testFunctionality = new HashSet<PendingCertificationResultTestFunctionalityEntity>();
        additionalSoftware = new HashSet<PendingCertificationResultAdditionalSoftwareEntity>();
        testProcedures = new HashSet<PendingCertificationResultTestProcedureEntity>();
        testData = new HashSet<PendingCertificationResultTestDataEntity>();
        testTools = new HashSet<PendingCertificationResultTestToolEntity>();
        testTasks = new HashSet<PendingCertificationResultTestTaskEntity>();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getPendingCertifiedProductId() {
        return pendingCertifiedProductId;
    }

    public void setPendingCertifiedProductId(final Long pendingCertifiedProductId) {
        this.pendingCertifiedProductId = pendingCertifiedProductId;
    }

    public Boolean getMeetsCriteria() {
        return meetsCriteria;
    }

    public void setMeetsCriteria(final Boolean meetsCriteria) {
        this.meetsCriteria = meetsCriteria;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public CertificationCriterionEntity getMappedCriterion() {
        return mappedCriterion;
    }

    public void setMappedCriterion(final CertificationCriterionEntity mappedCriterion) {
        this.mappedCriterion = mappedCriterion;
    }

    public Boolean getGap() {
        return gap;
    }

    public void setGap(final Boolean gap) {
        this.gap = gap;
    }

    public Boolean getSed() {
        return sed;
    }

    public void setSed(final Boolean sed) {
        this.sed = sed;
    }

    public Boolean getG1Success() {
        return g1Success;
    }

    public void setG1Success(final Boolean g1Success) {
        this.g1Success = g1Success;
    }

    public Boolean getG2Success() {
        return g2Success;
    }

    public void setG2Success(final Boolean g2Success) {
        this.g2Success = g2Success;
    }

    public Set<PendingCertificationResultTestStandardEntity> getTestStandards() {
        return testStandards;
    }

    public void setTestStandards(final Set<PendingCertificationResultTestStandardEntity> testStandards) {
        this.testStandards = testStandards;
    }

    public Set<PendingCertificationResultTestFunctionalityEntity> getTestFunctionality() {
        return testFunctionality;
    }

    public void setTestFunctionality(final Set<PendingCertificationResultTestFunctionalityEntity> testFunctionality) {
        this.testFunctionality = testFunctionality;
    }

    public Set<PendingCertificationResultAdditionalSoftwareEntity> getAdditionalSoftware() {
        return additionalSoftware;
    }

    public void setAdditionalSoftware(final Set<PendingCertificationResultAdditionalSoftwareEntity> additionalSoftware) {
        this.additionalSoftware = additionalSoftware;
    }

    public Set<PendingCertificationResultTestProcedureEntity> getTestProcedures() {
        return testProcedures;
    }

    public void setTestProcedures(final Set<PendingCertificationResultTestProcedureEntity> testProcedures) {
        this.testProcedures = testProcedures;
    }

    public Set<PendingCertificationResultTestDataEntity> getTestData() {
        return testData;
    }

    public void setTestData(final Set<PendingCertificationResultTestDataEntity> testData) {
        this.testData = testData;
    }

    public Set<PendingCertificationResultTestToolEntity> getTestTools() {
        return testTools;
    }

    public void setTestTools(final Set<PendingCertificationResultTestToolEntity> testTools) {
        this.testTools = testTools;
    }

    public Set<PendingCertificationResultUcdProcessEntity> getUcdProcesses() {
        return ucdProcesses;
    }

    public void setUcdProcesses(final Set<PendingCertificationResultUcdProcessEntity> ucdProcesses) {
        this.ucdProcesses = ucdProcesses;
    }

    public Boolean getHasAdditionalSoftware() {
        return hasAdditionalSoftware;
    }

    public void setHasAdditionalSoftware(final Boolean hasAdditionalSoftware) {
        this.hasAdditionalSoftware = hasAdditionalSoftware;
    }

    public Set<PendingCertificationResultTestTaskEntity> getTestTasks() {
        return testTasks;
    }

    public void setTestTasks(final Set<PendingCertificationResultTestTaskEntity> testTasks) {
        this.testTasks = testTasks;
    }

    public String getApiDocumentation() {
        return apiDocumentation;
    }

    public void setApiDocumentation(final String apiDocumentation) {
        this.apiDocumentation = apiDocumentation;
    }

    public Boolean getAttestationAnswer() {
        return attestationAnswer;
    }

    public void setAttestationAnswer(Boolean attestationAnswer) {
        this.attestationAnswer = attestationAnswer;
    }

    public String getExportDocumentation() {
        return exportDocumentation;
    }

    public void setExportDocumentation(String exportDocumentation) {
        this.exportDocumentation = exportDocumentation;
    }

    public String getDocumentationUrl() {
        return documentationUrl;
    }

    public void setDocumentationUrl(String documentationUrl) {
        this.documentationUrl = documentationUrl;
    }

    public String getUseCases() {
        return useCases;
    }

    public void setUseCases(String useCases) {
        this.useCases = useCases;
    }

    public String getServiceBaseUrlList() {
        return serviceBaseUrlList;
    }

    public void setServiceBaseUrlList(String serviceBaseUrlList) {
        this.serviceBaseUrlList = serviceBaseUrlList;
    }

    public String getPrivacySecurityFramework() {
        return privacySecurityFramework;
    }

    public void setPrivacySecurityFramework(final String privacySecurityFramework) {
        this.privacySecurityFramework = privacySecurityFramework;
    }
}
