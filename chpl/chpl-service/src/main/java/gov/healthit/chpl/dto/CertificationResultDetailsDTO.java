package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.domain.CertificationResultTestTool;
import gov.healthit.chpl.entity.listing.CertificationResultConformanceMethodEntity;
import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;
import gov.healthit.chpl.functionalityTested.CertificationResultFunctionalityTested;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.svap.domain.CertificationResultSvap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    private String serviceBaseUrlList;
    private String privacySecurityFramework;
    private CertificationCriterionDTO criterion;

    private List<CertificationResultOptionalStandard> optionalStandards;
    private List<CertificationResultFunctionalityTested> functionalitiesTested;
    private List<CertificationResultConformanceMethodEntity> conformanceMethods;
    private List<CertificationResultTestProcedureDTO> testProcedures;
    private List<CertificationResultTestDataDTO> testData;
    private List<CertificationResultTestTool> testTools;
    private List<CertificationResultTestStandardDTO> testStandards;
    private List<CertificationResultAdditionalSoftwareDTO> additionalSoftware;
    private List<CertificationResultSvap> svaps;

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
        this.serviceBaseUrlList = entity.getServiceBaseUrlList();
        this.privacySecurityFramework = entity.getPrivacySecurityFramework();
        if (entity.getCertificationCriterion() != null) {
            this.criterion = new CertificationCriterionDTO(entity.getCertificationCriterion());
        }

        if (entity.getCertificationResultOptionalStandards() != null) {
            this.optionalStandards = entity.getCertificationResultOptionalStandards().stream()
                    .map(e -> new CertificationResultOptionalStandard(e))
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultTestData() != null) {
            this.testData = entity.getCertificationResultTestData().stream()
                    .map(e -> new CertificationResultTestDataDTO(e))
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultFunctionalitiesTested() != null) {
            this.functionalitiesTested = entity.getCertificationResultFunctionalitiesTested().stream()
                    .map(e -> e.toDomain())
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultConformanceMethods() != null) {
            this.conformanceMethods = entity.getCertificationResultConformanceMethods().stream()
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultTestProcedures() != null) {
            this.testProcedures = entity.getCertificationResultTestProcedures().stream()
                    .map(e -> new CertificationResultTestProcedureDTO(e))
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultTestTools() != null) {
            this.testTools = entity.getCertificationResultTestTools().stream()
                    .map(e -> e.toDomain())
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultTestStandards() != null) {
            this.testStandards = entity.getCertificationResultTestStandards().stream()
                    .map(e -> new CertificationResultTestStandardDTO(e))
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultAdditionalSoftware() != null) {
            this.additionalSoftware = entity.getCertificationResultAdditionalSoftware().stream()
                    .map(e -> new CertificationResultAdditionalSoftwareDTO(e))
                    .collect(Collectors.toList());
        }

        if (entity.getCertificationResultSvaps() != null) {
            this.svaps = entity.getCertificationResultSvaps().stream()
                    .map(e -> new CertificationResultSvap(e))
                    .collect(Collectors.toList());
        }
    }
}
