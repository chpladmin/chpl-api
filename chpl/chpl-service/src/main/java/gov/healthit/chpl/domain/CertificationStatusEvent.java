package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationStatusEvent implements Serializable {
	private static final long serialVersionUID = -2498656549844148886L;

	/**
	 * Internal ID
	 */
	@XmlElement(required = true)
	private Long id;

	/**
	 * The date on which a change of certification status occurred.
	 */
	@XmlElement(required = true)
	private Long eventDate;

	/**
	 * Internal certification status ID.
	 */
	@XmlElement(required = true)
	private Long certificationStatusId;

	/**
	 * Certification status name.
	 */
	@XmlElement(required = true)
	private String certificationStatusName;

	@XmlTransient
	private Long lastModifiedUser;

	@XmlTransient
	private Long lastModifiedDate;

	public CertificationStatusEvent() {}

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
