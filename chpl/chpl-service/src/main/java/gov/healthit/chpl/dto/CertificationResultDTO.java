package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import gov.healthit.chpl.util.Util;

public class CertificationResultDTO implements Serializable {
    private static final long serialVersionUID = 4640517836460510236L;
    private Long id;
    private Long certificationCriterionId;
    private Long certifiedProductId;
    private Date creationDate;
    private Boolean deleted;
    private Boolean gap;
    private Boolean sed;
    private Boolean successful;
    private Boolean g1Success;
    private Boolean g2Success;
    private String apiDocumentation;
    private String privacySecurityFramework;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    private List<CertificationResultUcdProcessDTO> ucdProcesses;
    private List<CertificationResultTestFunctionalityDTO> testFunctionality;
    private List<CertificationResultTestProcedureDTO> testProcedures;
    private List<CertificationResultTestDataDTO> testData;
    private List<CertificationResultTestToolDTO> testTools;
    private List<CertificationResultTestStandardDTO> testStandards;
    private List<CertificationResultAdditionalSoftwareDTO> additionalSoftware;
    private List<CertificationResultTestTaskDTO> testTasks;
    private List<CertificationResultMacraMeasureDTO> g1Measures;
    private List<CertificationResultMacraMeasureDTO> g2Measures;

    public CertificationResultDTO() {
        ucdProcesses = new ArrayList<CertificationResultUcdProcessDTO>();
        additionalSoftware = new ArrayList<CertificationResultAdditionalSoftwareDTO>();
        testStandards = new ArrayList<CertificationResultTestStandardDTO>();
        testTools = new ArrayList<CertificationResultTestToolDTO>();
        testData = new ArrayList<CertificationResultTestDataDTO>();
        testProcedures = new ArrayList<CertificationResultTestProcedureDTO>();
        testFunctionality = new ArrayList<CertificationResultTestFunctionalityDTO>();
        testTasks = new ArrayList<CertificationResultTestTaskDTO>();
        g1Measures = new ArrayList<CertificationResultMacraMeasureDTO>();
        g2Measures = new ArrayList<CertificationResultMacraMeasureDTO>();
    }

    public CertificationResultDTO(CertificationResultEntity entity) {
        this();
        this.id = entity.getId();
        this.certificationCriterionId = entity.getCertificationCriterionId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.creationDate = entity.getCreationDate();
        this.gap = entity.isGap();
        this.sed = entity.getSed();
        this.g1Success = entity.getG1Success();
        this.g2Success = entity.getG2Success();
        this.apiDocumentation = entity.getApiDocumentation();
        this.privacySecurityFramework = entity.getPrivacySecurityFramework();
        this.successful = entity.isSuccess();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }

    public CertificationResultDTO(CertificationResult domain) {
        this();
        this.gap = domain.isGap();
        this.sed = domain.isSed();
        this.g1Success = domain.isG1Success();
        this.g2Success = domain.isG2Success();
        this.apiDocumentation = domain.getApiDocumentation();
        this.privacySecurityFramework = domain.getPrivacySecurityFramework();
        this.successful = domain.isSuccess();
    }

    public Long getCertificationCriterionId() {
        return certificationCriterionId;
    }

    public void setCertificationCriterionId(final Long certificationCriterionId) {
        this.certificationCriterionId = certificationCriterionId;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProduct(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
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

    public Boolean getGap() {
        return gap;
    }

    public void setGap(final Boolean gap) {
        this.gap = gap;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public Boolean getSuccessful() {
        return successful;
    }

    public void setSuccessful(final Boolean successful) {
        this.successful = successful;
    }

    public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftware() {
        return additionalSoftware;
    }

    public void setAdditionalSoftware(final List<CertificationResultAdditionalSoftwareDTO> list) {
        this.additionalSoftware = list;
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

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public List<CertificationResultTestStandardDTO> getTestStandards() {
        return testStandards;
    }

    public void setTestStandards(final List<CertificationResultTestStandardDTO> testStandards) {
        this.testStandards = testStandards;
    }

    public List<CertificationResultTestToolDTO> getTestTools() {
        return testTools;
    }

    public void setTestTools(final List<CertificationResultTestToolDTO> testTools) {
        this.testTools = testTools;
    }

    public List<CertificationResultTestDataDTO> getTestData() {
        return testData;
    }

    public void setTestData(final List<CertificationResultTestDataDTO> testData) {
        this.testData = testData;
    }

    public List<CertificationResultTestFunctionalityDTO> getTestFunctionality() {
        return testFunctionality;
    }

    public void setTestFunctionality(final List<CertificationResultTestFunctionalityDTO> testFunctionality) {
        this.testFunctionality = testFunctionality;
    }

    public List<CertificationResultUcdProcessDTO> getUcdProcesses() {
        return ucdProcesses;
    }

    public void setUcdProcesses(final List<CertificationResultUcdProcessDTO> ucdProcesses) {
        this.ucdProcesses = ucdProcesses;
    }

    public List<CertificationResultTestTaskDTO> getTestTasks() {
        return testTasks;
    }

    public void setTestTasks(final List<CertificationResultTestTaskDTO> testTasks) {
        this.testTasks = testTasks;
    }

    public String getApiDocumentation() {
        return apiDocumentation;
    }

    public void setApiDocumentation(final String apiDocumentation) {
        this.apiDocumentation = apiDocumentation;
    }

    public String getPrivacySecurityFramework() {
        return privacySecurityFramework;
    }

    public void setPrivacySecurityFramework(final String privacySecurityFramework) {
        this.privacySecurityFramework = privacySecurityFramework;
    }

    public List<CertificationResultMacraMeasureDTO> getG1Measures() {
        return g1Measures;
    }

    public void setG1Measures(final List<CertificationResultMacraMeasureDTO> g1Measures) {
        this.g1Measures = g1Measures;
    }

    public List<CertificationResultMacraMeasureDTO> getG2Measures() {
        return g2Measures;
    }

    public void setG2Measures(final List<CertificationResultMacraMeasureDTO> g2Measures) {
        this.g2Measures = g2Measures;
    }

    public List<CertificationResultTestProcedureDTO> getTestProcedures() {
        return testProcedures;
    }

    public void setTestProcedures(final List<CertificationResultTestProcedureDTO> testProcedures) {
        this.testProcedures = testProcedures;
    }

}
