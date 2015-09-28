package gov.healthit.chpl.domain;


import gov.healthit.chpl.dto.CertificationEventDTO;

public class CertificationEvent {

	private Long id;
	private String city;
	private String eventDate;
	private Long lastModifiedUser;
	private String lastModifiedDate;
	private String state;
	private Long eventTypeId;
	private String eventTypeDescription;
	private String eventTypeName;
	
	public CertificationEvent(){}
	
	public CertificationEvent(CertificationEventDTO dto){
		
		this.id = dto.getId();
		this.city = dto.getCity();
		this.eventDate = dto.getEventDate().getTime() + "";
		this.lastModifiedUser = dto.getLastModifiedUser();
		this.lastModifiedDate = dto.getLastModifiedDate().getTime() + "";
		this.state = dto.getState();
		this.eventTypeId = dto.getEventTypeId();
		this.eventTypeDescription = dto.getEventTypeDTO().getDescription();
		this.eventTypeName = dto.getEventTypeDTO().getName();
		
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
	public String getEventDate() {
		return eventDate;
	}
	public void setEventDate(String eventDate) {
		this.eventDate = eventDate;
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
	public String getEventTypeName() {
		return eventTypeName;
	}
	public void setEventTypeName(String eventTypeName) {
		this.eventTypeName = eventTypeName;
	}
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	public String getEventTypeDescription() {
		return eventTypeDescription;
	}
	public void setEventTypeDescription(String eventTypeDescription) {
		this.eventTypeDescription = eventTypeDescription;
	}
}
