package gov.healthit.chpl.dto;


import java.util.Date;

import gov.healthit.chpl.entity.CertificationEventEntity;

public class CertificationEventDTO {
	
	private Long id;
	private Long certifiedProductId;
	private String city;
	private Date creationDate;
	private Boolean deleted;
	private Date eventDate;
	private Date lastModifiedDate;
	private Long lastModifiedUser;
	private String state;
	private Long eventTypeId;
	
	
	public CertificationEventDTO(){}
	
	public CertificationEventDTO(CertificationEventEntity entity){
		
		this.id = entity.getId();
		this.certifiedProductId = entity.getCertifiedProductId();
		this.city = entity.getCity();
		this.creationDate = entity.getCreationDate();
		this.deleted = entity.getDeleted();
		this.eventDate = entity.getEventDate();
		this.lastModifiedDate = entity.getLastModifiedDate();
		this.lastModifiedUser = entity.getLastModifiedUser();
		this.state = entity.getState();
		this.eventTypeId = entity.getEventTypeId();
		
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
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
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
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
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Long getEventTypeId() {
		return eventTypeId;
	}
	public void setEventTypeId(Long eventTypeId) {
		this.eventTypeId = eventTypeId;
	}

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}
	
}