package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.healthit.chpl.entity.listing.CQMResultEntity;
import gov.healthit.chpl.util.Util;

public class CQMResultDTO implements Serializable {
    private static final long serialVersionUID = 314245521842632450L;
    private Long id;
    private Long cqmCriterionId;
    private Long certifiedProductId;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Boolean success;
    private Boolean deleted;

    private List<CQMResultCriteriaDTO> criteria;

    public CQMResultDTO() {
        criteria = new ArrayList<CQMResultCriteriaDTO>();
    }

    public CQMResultDTO(CQMResultEntity entity) {
        this();
        this.id = entity.getId();
        this.cqmCriterionId = entity.getCqmCriterionId();
        this.certifiedProductId = entity.getCertifiedProductId();
        this.creationDate = entity.getCreationDate();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.success = entity.getSuccess();
        this.deleted = entity.getDeleted();
    }

    public Long getCqmCriterionId() {
        return cqmCriterionId;
    }

    public void setCqmCriterionId(final Long cqmCriterionId) {
        this.cqmCriterionId = cqmCriterionId;
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

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(final Boolean success) {
        this.success = success;
    }

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public List<CQMResultCriteriaDTO> getCriteria() {
        return criteria;
    }

    public void setCriteria(final List<CQMResultCriteriaDTO> criteria) {
        this.criteria = criteria;
    }

}
