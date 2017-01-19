package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.dto.MeaningfulUseAccurateAsOfDTO;

public class MeaningfulUseAccurateAsOf implements Serializable {
	private static final long serialVersionUID = -4803763243075068608L;
	
	private Long id;
	private Date accurateAsOfDate;
	private Boolean deleted;
	private Long lastModifiedUser;
	private Date creationDate;
	private Date lastModifiedDate;
	
	public MeaningfulUseAccurateAsOf(){};
	
	public MeaningfulUseAccurateAsOf(MeaningfulUseAccurateAsOfDTO muuDTO){
		this.id = muuDTO.getId();
		this.accurateAsOfDate = muuDTO.getAccurateAsOfDate();
		this.deleted = muuDTO.getDeleted();
		this.lastModifiedUser = muuDTO.getLastModifiedUser();
		this.creationDate = muuDTO.getCreationDate();
		this.lastModifiedDate = muuDTO.getLastModifiedDate();
	};
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getAccurateAsOfDate() {
		return accurateAsOfDate;
	}

	public void setAccurateAsOfDate(Date accurateAsOfDate) {
		this.accurateAsOfDate = accurateAsOfDate;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}

	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}

	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

}
