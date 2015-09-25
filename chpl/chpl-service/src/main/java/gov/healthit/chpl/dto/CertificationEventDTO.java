package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.CertificationEventEntity;

public class CertificationEventDTO {
	
	private Long id;
	private Long eventTypeId;
	private String eventTypeName;
	private Long certifiedProductId;
	private Date eventDate;
	private String city;
	private String state;
	
	public CertificationEventDTO(){}
	public CertificationEventDTO(CertificationEventEntity entity){
		this.id = entity.getId();
		this.eventTypeId = entity.getEventType().getId();
		this.eventTypeName = entity.getEventType().getName();
		this.certifiedProductId = entity.getCertifiedProductId();
		this.eventDate = entity.getEventDate();
		this.city = entity.getCity();
		this.state = entity.getState();
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getEventTypeId() {
		return eventTypeId;
	}
	public void setEventTypeId(Long eventTypeId) {
		this.eventTypeId = eventTypeId;
	}
	public String getEventTypeName() {
		return eventTypeName;
	}
	public void setEventTypeName(String eventTypeName) {
		this.eventTypeName = eventTypeName;
	}
	public Date getEventDate() {
		return eventDate;
	}
	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Long getCertifiedProductId() {
		return certifiedProductId;
	}
	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}
}
