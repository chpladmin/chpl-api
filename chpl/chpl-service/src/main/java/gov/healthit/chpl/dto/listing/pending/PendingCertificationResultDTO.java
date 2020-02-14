package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultG1MacraMeasureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultG2MacraMeasureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestDataEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestFunctionalityEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestTaskEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestToolEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultUcdProcessEntity;

public class PendingCertificationResultDTO implements Serializable {
    private static final long serialVersionUID = -1026669045107851464L;
    private Long id;
    private Long pendingCertifiedProductId;
    private Boolean meetsCriteria;
    private Boolean gap;
    private Boolean sed;
    private Boolean g1Success;
    private Boolean g2Success;
    private Boolean attestationAnswer;
    private String apiDocumentation;
    private String exportDocumentation;
    private String documentationUrl;
    private String useCases;

    private String privacySecurityFramework;
    private CertificationCriterionDTO criterion;

    private List<PendingCertificationResultUcdProcessDTO> ucdProcesses;
    private List<PendingCertificationResultAdditionalSoftwareDTO> additionalSoftware;
    private List<PendingCertificationResultTestDataDTO> testData;
    private List<PendingCertificationResultTestFunctionalityDTO> testFunctionality;
    private List<PendingCertificationResultTestProcedureDTO> testProcedures;
    private List<PendingCertificationResultTestStandardDTO> testStandards;
    private List<PendingCertificationResultTestToolDTO> testTools;
    private List<PendingCertificationResultMacraMeasureDTO> g1MacraMeasures;
    private List<PendingCertificationResultMacraMeasureDTO> g2MacraMeasures;
    private List<PendingCertificationResultTestTaskDTO> testTasks;

    public PendingCertificationResultDTO() {
        criterion = new CertificationCriterionDTO();
        ucdProcesses = new ArrayList<PendingCertificationResultUcdProcessDTO>();
        additionalSoftware = new ArrayList<PendingCertificationResultAdditionalSoftwareDTO>();
        testData = new ArrayList<PendingCertificationResultTestDataDTO>();
        testFunctionality = new ArrayList<PendingCertificationResultTestFunctionalityDTO>();
        testProcedures = new ArrayList<PendingCertificationResultTestProcedureDTO>();
        testStandards = new ArrayList<PendingCertificationResultTestStandardDTO>();
        testTools = new ArrayList<PendingCertificationResultTestToolDTO>();
        g1MacraMeasures = new ArrayList<PendingCertificationResultMacraMeasureDTO>();
        g2MacraMeasures = new ArrayList<PendingCertificationResultMacraMeasureDTO>();
        testTasks = new ArrayList<PendingCertificationResultTestTaskDTO>();
    }

    public PendingCertificationResultDTO(final PendingCertificationResultEntity entity) {
        this();
        this.setId(entity.getId());

        if (entity.getMappedCriterion() != null) {
            this.criterion = new CertificationCriterionDTO(entity.getMappedCriterion());
        }

        this.setPendingCertifiedProductId(entity.getPendingCertifiedProductId());
        this.setMeetsCriteria(entity.getMeetsCriteria());
        this.setGap(entity.getGap());
        this.setSed(entity.getSed());
        this.setG1Success(entity.getG1Success());
        this.setG2Success(entity.getG2Success());
        this.attestationAnswer = entity.getAttestationAnswer();
        this.apiDocumentation = entity.getApiDocumentation();
        this.exportDocumentation = entity.getExportDocumentation();
        this.documentationUrl = entity.getDocumentationUrl();
        this.useCases = entity.getUseCases();
        this.privacySecurityFramework = entity.getPrivacySecurityFramework();

        if (entity.getUcdProcesses() != null && entity.getUcdProcesses().size() > 0) {
            for (PendingCertificationResultUcdProcessEntity e : entity.getUcdProcesses()) {
                this.getUcdProcesses().add(new PendingCertificationResultUcdProcessDTO(e));
            }
        }

        if (entity.getTestStandards() != null) {
            for (PendingCertificationResultTestStandardEntity e : entity.getTestStandards()) {
                this.getTestStandards().add(new PendingCertificationResultTestStandardDTO(e));
            }
        }
        if (entity.getTestFunctionality() != null) {
            for (PendingCertificationResultTestFunctionalityEntity e : entity.getTestFunctionality()) {
                this.getTestFunctionality().add(new PendingCertificationResultTestFunctionalityDTO(e));
            }
        }
        if (entity.getAdditionalSoftware() != null) {
            for (PendingCertificationResultAdditionalSoftwareEntity e : entity.getAdditionalSoftware()) {
                this.getAdditionalSoftware().add(new PendingCertificationResultAdditionalSoftwareDTO(e));
            }
        }
        if (entity.getTestProcedures() != null) {
            for (PendingCertificationResultTestProcedureEntity e : entity.getTestProcedures()) {
                this.getTestProcedures().add(new PendingCertificationResultTestProcedureDTO(e));
            }
        }
        if (entity.getTestData() != null) {
            for (PendingCertificationResultTestDataEntity e : entity.getTestData()) {
                this.getTestData().add(new PendingCertificationResultTestDataDTO(e));
            }
        }
        if (entity.getTestTools() != null) {
            for (PendingCertificationResultTestToolEntity e : entity.getTestTools()) {
                this.getTestTools().add(new PendingCertificationResultTestToolDTO(e));
            }
        }

        if (entity.getG1MacraMeasures() != null) {
            for (PendingCertificationResultG1MacraMeasureEntity e : entity.getG1MacraMeasures()) {
                this.getG1MacraMeasures().add(new PendingCertificationResultMacraMeasureDTO(e));
            }
        }

        if (entity.getG2MacraMeasures() != null) {
            for (PendingCertificationResultG2MacraMeasureEntity e : entity.getG2MacraMeasures()) {
                this.getG2MacraMeasures().add(new PendingCertificationResultMacraMeasureDTO(e));
            }
        }

        if (entity.getTestTasks() != null && entity.getTestTasks().size() > 0) {
            for (PendingCertificationResultTestTaskEntity e : entity.getTestTasks()) {
                PendingCertificationResultTestTaskDTO taskDto = new PendingCertificationResultTestTaskDTO(e);
                this.getTestTasks().add(taskDto);
            }
        }
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

    public List<PendingCertificationResultAdditionalSoftwareDTO> getAdditionalSoftware() {
        return additionalSoftware;
    }

    public void setAdditionalSoftware(final List<PendingCertificationResultAdditionalSoftwareDTO> additionalSoftware) {
        this.additionalSoftware = additionalSoftware;
    }

    public List<PendingCertificationResultTestDataDTO> getTestData() {
        return testData;
    }

    public void setTestData(final List<PendingCertificationResultTestDataDTO> testData) {
        this.testData = testData;
    }

    public List<PendingCertificationResultTestFunctionalityDTO> getTestFunctionality() {
        return testFunctionality;
    }

    public void setTestFunctionality(final List<PendingCertificationResultTestFunctionalityDTO> testFunctionality) {
        this.testFunctionality = testFunctionality;
    }

    public List<PendingCertificationResultTestProcedureDTO> getTestProcedures() {
        return testProcedures;
    }

    public void setTestProcedures(final List<PendingCertificationResultTestProcedureDTO> testProcedures) {
        this.testProcedures = testProcedures;
    }

    public List<PendingCertificationResultTestStandardDTO> getTestStandards() {
        return testStandards;
    }

    public void setTestStandards(final List<PendingCertificationResultTestStandardDTO> testStandards) {
        this.testStandards = testStandards;
    }

    public List<PendingCertificationResultTestToolDTO> getTestTools() {
        return testTools;
    }

    public void setTestTools(final List<PendingCertificationResultTestToolDTO> testTools) {
        this.testTools = testTools;
    }

    public List<PendingCertificationResultUcdProcessDTO> getUcdProcesses() {
        return ucdProcesses;
    }

    public void setUcdProcesses(final List<PendingCertificationResultUcdProcessDTO> ucdProcesses) {
        this.ucdProcesses = ucdProcesses;
    }

    public List<PendingCertificationResultTestTaskDTO> getTestTasks() {
        return testTasks;
    }

    public void setTestTasks(final List<PendingCertificationResultTestTaskDTO> testTasks) {
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

    public String getPrivacySecurityFramework() {
        return privacySecurityFramework;
    }

    public void setPrivacySecurityFramework(final String privacySecurityFramework) {
        this.privacySecurityFramework = privacySecurityFramework;
    }

    public List<PendingCertificationResultMacraMeasureDTO> getG1MacraMeasures() {
        return g1MacraMeasures;
    }

    public void setG1MacraMeasures(final List<PendingCertificationResultMacraMeasureDTO> g1Measures) {
        this.g1MacraMeasures = g1Measures;
    }

    public List<PendingCertificationResultMacraMeasureDTO> getG2MacraMeasures() {
        return g2MacraMeasures;
    }

    public void setG2MacraMeasures(final List<PendingCertificationResultMacraMeasureDTO> g2Measures) {
        this.g2MacraMeasures = g2Measures;
    }

    public CertificationCriterionDTO getCriterion() {
        return criterion;
    }

    public void setCriterion(final CertificationCriterionDTO criterion) {
        this.criterion = criterion;
    }
}
