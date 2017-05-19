package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CertifiedProductTargetedUserDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertifiedProductTargetedUser implements Serializable {
	private static final long serialVersionUID = -2078691100124619582L;
	
	@XmlElement(required = true)
	private Long id;
	
	@XmlElement(required = true)
	private Long targetedUserId;
	
	@XmlElement(required = true)
	private String targetedUserName;

	public CertifiedProductTargetedUser() {
		super();
	}
	
	public CertifiedProductTargetedUser(CertifiedProductTargetedUserDTO dto) {
		this.id = dto.getId();
		this.targetedUserId = dto.getTargetedUserId();
		this.targetedUserName = dto.getTargetedUserName();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTargetedUserId() {
		return targetedUserId;
	}

	public void setTargetedUserId(Long targetedUserId) {
		this.targetedUserId = targetedUserId;
	}

	public String getTargetedUserName() {
		return targetedUserName;
	}

	public void setTargetedUserName(String targetedUserName) {
		this.targetedUserName = targetedUserName;
	}

}
