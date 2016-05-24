package gov.healthit.chpl.dto;

import gov.healthit.chpl.entity.CQMResultEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CQMResultDTO {
	
	private Long id;
	private Long cqmCriterionId;
	private Long certifiedProductId;
	private Date creationDate;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private Boolean success;
	private Boolean deleted;
	
	private List<CQMResultCriteriaDTO> criteria;
	
	public CQMResultDTO(){
		criteria = new ArrayList<CQMResultCriteriaDTO>();
	}
	
	public CQMResultDTO(CQMResultEntity entity){
		this();
		this.id = entity.getId();
		this.cqmCriterionId = entity.getCqmCriterionId();
		this.certifiedProductId = entity.getCertifiedProductId();
		this.creationDate = entity.getCreationDate();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.success = entity.isSuccess();
		this.deleted = entity.isDeleted();
	}
	
	public Long getCqmCriterionId() {
		return cqmCriterionId;
	}
	public void setCqmCriterionId(Long cqmCriterionId) {
		this.cqmCriterionId = cqmCriterionId;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Boolean getDeleted() {
		return deleted;
	}
	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}
	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
	public Boolean getSuccess() {
		return success;
	}
	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public List<CQMResultCriteriaDTO> getCriteria() {
		return criteria;
	}

	public void setCriteria(List<CQMResultCriteriaDTO> criteria) {
		this.criteria = criteria;
	}

}
