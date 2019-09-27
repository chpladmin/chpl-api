package gov.healthit.chpl.dto;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.CertificationResultDetailsEntity;

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
    private String apiDocumentation;
    private String privacySecurityFramework;

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
        this.apiDocumentation = entity.getApiDocumentation();
        this.privacySecurityFramework = entity.getPrivacySecurityFramework();
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
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
}
