package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.entity.listing.CQMResultDetailsEntity;

public class CQMResultDetailsDTO implements Serializable {
    private static final long serialVersionUID = 7190018955877689390L;
    private Long id;
    private Boolean success;
    private Long cqmCriterionId;
    private String number;
    private String cmsId;
    private String title;
    private String description;
    private String nqfNumber;
    private Long cqmCriterionTypeId;
    private String domain;
    private Long cqmVersionId;
    private String version;

    private List<CQMResultCertification> criteria;

    public CQMResultDetailsDTO() {
        criteria = new ArrayList<CQMResultCertification>();
    }

    public CQMResultDetailsDTO(CQMResultDetailsEntity entity) {
        this();
        this.id = entity.getId();
        this.success = entity.getSuccess();
        this.cqmCriterionId = entity.getCqmCriterionId();
        this.number = entity.getNumber();
        this.cmsId = entity.getCmsId();
        this.title = entity.getTitle();
        this.description = entity.getDescription();
        this.nqfNumber = entity.getNqfNumber();
        this.cqmCriterionTypeId = entity.getCqmCriterionTypeId();
        this.domain = entity.getDomain();
        this.cqmVersionId = entity.getCqmVersionId();
        this.version = entity.getVersion();

    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public Long getCqmCriterionId() {
        return cqmCriterionId;
    }

    public void setCqmCriterionId(final Long cqmCriterionId) {
        this.cqmCriterionId = cqmCriterionId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(final String number) {
        this.number = number;
    }

    public String getCmsId() {
        return cmsId;
    }

    public void setCmsId(final String cmsId) {
        this.cmsId = cmsId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getNqfNumber() {
        return nqfNumber;
    }

    public void setNqfNumber(final String nqfNumber) {
        this.nqfNumber = nqfNumber;
    }

    public Long getCqmCriterionTypeId() {
        return cqmCriterionTypeId;
    }

    public void setCqmCriterionTypeId(final Long cqmCriterionTypeId) {
        this.cqmCriterionTypeId = cqmCriterionTypeId;
    }

    public Long getCqmVersionId() {
        return cqmVersionId;
    }

    public void setCqmVersionId(final Long cqmVersionId) {
        this.cqmVersionId = cqmVersionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public List<CQMResultCertification> getCriteria() {
        return criteria;
    }

    public void setCriteria(final List<CQMResultCertification> criteria) {
        this.criteria = criteria;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

}
