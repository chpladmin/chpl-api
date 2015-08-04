package gov.healthit.chpl.dto;

import java.util.Date;

public class CQMResultDTO {
	
	private Long cqmCriterionId;
	private Long cqmVersionId;
	private Date creationDate;
	private Boolean deleted;
	private Long id;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private Boolean success;
	public Long getCqmCriterionId() {
		return cqmCriterionId;
	}
	public void setCqmCriterionId(Long cqmCriterionId) {
		this.cqmCriterionId = cqmCriterionId;
	}
	public Long getCqmVersionId() {
		return cqmVersionId;
	}
	public void setCqmVersionId(Long cqmVersionId) {
		this.cqmVersionId = cqmVersionId;
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

}
