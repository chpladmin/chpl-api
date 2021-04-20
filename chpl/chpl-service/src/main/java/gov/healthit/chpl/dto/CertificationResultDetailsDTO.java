package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CertificationResultDetailsDTO implements Serializable {
    private static final long serialVersionUID = 4560202421131481086L;
    private Long id;
    private Long certificationCriterionId;
    private Long certifiedProductId;
    private Boolean success;
    private String number;
    private String title;
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

    private List<CertificationResultTestFunctionalityDTO> testFunctionality;
    private List<CertificationResultTestProcedureDTO> testProcedures;
    private List<CertificationResultTestDataDTO> testData;
    private List<CertificationResultTestToolDTO> testTools;
    private List<CertificationResultTestStandardDTO> testStandards;
    private List<CertificationResultAdditionalSoftwareDTO> additionalSoftware;

    public CertificationResultDetailsDTO(final CertificationResultDetailsEntity entity) {
        this.id = entity.getId();
        this.certificationCriterionId = entity.getCertificationCriterionId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.success = entity.getSuccess();
        this.number = entity.getNumber();
        this.title = entity.getTitle();
        this.gap = entity.getGap();
        this.sed = entity.getSed();
        this.g1Success = entity.getG1Success();
        this.g2Success = entity.getG2Success();
        this.attestationAnswer = entity.getAttestationAnswer();
        this.apiDocumentation = entity.getApiDocumentation();
        this.exportDocumentation = entity.getExportDocumentation();
        this.documentationUrl = entity.getDocumentationUrl();
        this.useCases = entity.getUseCases();
        this.privacySecurityFramework = entity.getPrivacySecurityFramework();
        if (entity.getCertificationCriterion() != null) {
            this.criterion = new CertificationCriterionDTO(entity.getCertificationCriterion());
        }

        if (entity.getCertificationResultTestData() != null) {
            this.testData = entity.getCertificationResultTestData().stream()
                    .filter(e -> e != null && e.getDeleted() != null && !e.getDeleted())
                    .map(e -> new CertificationResultTestDataDTO(e))
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultTestFunctionalities() != null) {
            this.testFunctionality = entity.getCertificationResultTestFunctionalities().stream()
                    .filter(e -> e != null && e.getDeleted() != null && !e.getDeleted())
                    .map(e -> new CertificationResultTestFunctionalityDTO(e))
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultTestProcedures() != null) {
            this.testProcedures = entity.getCertificationResultTestProcedures().stream()
                    .filter(e -> e != null && e.getDeleted() != null && !e.getDeleted())
                    .map(e -> new CertificationResultTestProcedureDTO(e))
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultTestTools() != null) {
            this.testTools = entity.getCertificationResultTestTools().stream()
                    .filter(e -> e != null && e.getDeleted() != null && !e.getDeleted())
                    .map(e -> new CertificationResultTestToolDTO(e))
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultTestStandards() != null) {
            this.testStandards = entity.getCertificationResultTestStandards().stream()
                    .filter(e -> e != null && e.getDeleted() != null && !e.getDeleted())
                    .map(e -> new CertificationResultTestStandardDTO(e))
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultAdditionalSoftware() != null) {
            this.additionalSoftware = entity.getCertificationResultAdditionalSoftware().stream()
                    .filter(e -> e != null && e.getDeleted() != null && !e.getDeleted())
                    .map(e -> new CertificationResultAdditionalSoftwareDTO(e))
                    .collect(Collectors.toList());
        }
    }
}
