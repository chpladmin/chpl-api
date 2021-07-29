package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultAdditionalSoftwareEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultOptionalStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestDataEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestFunctionalityEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestProcedureEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestStandardEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestTaskEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultTestToolEntity;
import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultUcdProcessEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Builder
@Data
@AllArgsConstructor
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
    private String serviceBaseUrlList;

    private String privacySecurityFramework;
    private CertificationCriterionDTO criterion;

    @Singular
    private List<PendingCertificationResultUcdProcessDTO> ucdProcesses;

    @Singular("additionalSoftwareSingle")
    private List<PendingCertificationResultAdditionalSoftwareDTO> additionalSoftware;

    @Singular
    private List<PendingCertificationResultOptionalStandardDTO> optionalStandards;

    @Singular("testDataSingle")
    private List<PendingCertificationResultTestDataDTO> testData;

    @Singular("testFunctionalitySingle")
    private List<PendingCertificationResultTestFunctionalityDTO> testFunctionality;

    @Singular
    private List<PendingCertificationResultTestProcedureDTO> testProcedures;

    @Singular
    private List<PendingCertificationResultTestStandardDTO> testStandards;

    @Singular
    private List<PendingCertificationResultTestToolDTO> testTools;

    @Singular
    private List<PendingCertificationResultTestTaskDTO> testTasks;

    public PendingCertificationResultDTO() {
        criterion = new CertificationCriterionDTO();
        ucdProcesses = new ArrayList<PendingCertificationResultUcdProcessDTO>();
        additionalSoftware = new ArrayList<PendingCertificationResultAdditionalSoftwareDTO>();
        optionalStandards = new ArrayList<PendingCertificationResultOptionalStandardDTO>();
        testData = new ArrayList<PendingCertificationResultTestDataDTO>();
        testFunctionality = new ArrayList<PendingCertificationResultTestFunctionalityDTO>();
        testProcedures = new ArrayList<PendingCertificationResultTestProcedureDTO>();
        testStandards = new ArrayList<PendingCertificationResultTestStandardDTO>();
        testTools = new ArrayList<PendingCertificationResultTestToolDTO>();
        testTasks = new ArrayList<PendingCertificationResultTestTaskDTO>();
    }

    public PendingCertificationResultDTO(PendingCertificationResultEntity entity) {
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
        this.serviceBaseUrlList = entity.getServiceBaseUrlList();
        this.privacySecurityFramework = entity.getPrivacySecurityFramework();

        if (entity.getUcdProcesses() != null && entity.getUcdProcesses().size() > 0) {
            for (PendingCertificationResultUcdProcessEntity e : entity.getUcdProcesses()) {
                this.getUcdProcesses().add(new PendingCertificationResultUcdProcessDTO(e));
            }
        }

        if (entity.getOptionalStandards() != null) {
            for (PendingCertificationResultOptionalStandardEntity e : entity.getOptionalStandards()) {
                this.getOptionalStandards().add(new PendingCertificationResultOptionalStandardDTO(e));
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

        if (entity.getTestTasks() != null && entity.getTestTasks().size() > 0) {
            for (PendingCertificationResultTestTaskEntity e : entity.getTestTasks()) {
                PendingCertificationResultTestTaskDTO taskDto = new PendingCertificationResultTestTaskDTO(e);
                this.getTestTasks().add(taskDto);
            }
        }
    }
}
