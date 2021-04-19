package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.entity.listing.CertificationResultEntity;
import lombok.Data;

@Data
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
    private Boolean attestationAnswer;
    private String apiDocumentation;
    private String exportDocumentation;
    private String documentationUrl;
    private String useCases;
    private String serviceBaseUrlList;
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

    public CertificationResultDTO() {
        ucdProcesses = new ArrayList<CertificationResultUcdProcessDTO>();
        additionalSoftware = new ArrayList<CertificationResultAdditionalSoftwareDTO>();
        testStandards = new ArrayList<CertificationResultTestStandardDTO>();
        testTools = new ArrayList<CertificationResultTestToolDTO>();
        testData = new ArrayList<CertificationResultTestDataDTO>();
        testProcedures = new ArrayList<CertificationResultTestProcedureDTO>();
        testFunctionality = new ArrayList<CertificationResultTestFunctionalityDTO>();
        testTasks = new ArrayList<CertificationResultTestTaskDTO>();
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
        this.attestationAnswer = entity.getAttestationAnswer();
        this.apiDocumentation = entity.getApiDocumentation();
        this.exportDocumentation = entity.getExportDocumentation();
        this.documentationUrl = entity.getDocumentationUrl();
        this.useCases = entity.getUseCases();
        this.serviceBaseUrlList = entity.getServiceBaseUrlList();
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
        this.attestationAnswer = domain.getAttestationAnswer();
        this.apiDocumentation = domain.getApiDocumentation();
        this.exportDocumentation = domain.getExportDocumentation();
        this.documentationUrl = domain.getDocumentationUrl();
        this.useCases = domain.getUseCases();
        this.serviceBaseUrlList = domain.getServiceBaseUrlList();
        this.privacySecurityFramework = domain.getPrivacySecurityFramework();
        this.successful = domain.isSuccess();
    }
}
