package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.DeveloperStatusEventDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class DeveloperStatusEvent implements Serializable {
	private static final long serialVersionUID = -7303257499336378800L;
	
	/**
	 * Developer status event internal ID
	 */
	@XmlElement(required = false, nillable=true)
	private Long id;
	
	/**
	 * Developer internal ID
	 */
	@XmlElement(required = true)
	private Long developerId;
	
	/**
	 * The status the developer changed TO with this status event.
	 */
	@XmlElement(required = true)
	private DeveloperStatus status;
	
	/**
	 * Date this status event occurred.
	 */
	@XmlElement(required = true)
	private Date statusDate;
	
	public DeveloperStatusEvent() {
	}
	
	public DeveloperStatusEvent(DeveloperStatusEventDTO dto) {
		this.id = dto.getId();
		this.developerId = dto.getDeveloperId();
		this.status = new DeveloperStatus(dto.getStatus());
		this.statusDate = dto.getStatusDate();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDeveloperId() {
		return developerId;
	}

	public void setDeveloperId(Long developerId) {
		this.developerId = developerId;
	}

	public DeveloperStatus getStatus() {
		return status;
	}

	public void setStatus(DeveloperStatus status) {
		this.status = status;
	}

	public Date getStatusDate() {
		return statusDate;
	}

	public void setStatusDate(Date statusDate) {
		this.statusDate = statusDate;
	}
}
