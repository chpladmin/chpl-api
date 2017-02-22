package gov.healthit.chpl.domain;

import java.io.Serializable;

public class CertificationStatusEvent implements Serializable {
	private static final long serialVersionUID = -2498656549844148886L;
	private Long id;
	private Long eventDate;
	private Long certificationStatusId;
	private String certificationStatusName;
	private Long lastModifiedUser;
	private Long lastModifiedDate;
	
	public CertificationStatusEvent(){}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getEventDate() {
		return eventDate;
	}
	public void setEventDate(Long eventDate) {
		this.eventDate = eventDate;
	}
	public Long getLastModifiedUser() {
		return lastModifiedUser;
	}
	public void setLastModifiedUser(Long lastModifiedUser) {
		this.lastModifiedUser = lastModifiedUser;
	}
	public Long getLastModifiedDate() {
		return lastModifiedDate;
	}
	public void setLastModifiedDate(Long lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Long getCertificationStatusId() {
		return certificationStatusId;
	}

	public void setCertificationStatusId(Long certificationStatusId) {
		this.certificationStatusId = certificationStatusId;
	}

	public String getCertificationStatusName() {
		return certificationStatusName;
	}

	public void setCertificationStatusName(String certificationStatusName) {
		this.certificationStatusName = certificationStatusName;
	}
}
