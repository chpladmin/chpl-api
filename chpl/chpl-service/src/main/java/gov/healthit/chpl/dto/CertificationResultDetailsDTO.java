package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;
import lombok.Data;

@Data
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

    public CertificationResultDetailsDTO() {
    }

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
    }
}
