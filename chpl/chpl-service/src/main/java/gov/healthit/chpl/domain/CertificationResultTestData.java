package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CertificationResultTestDataDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResultTestData implements Serializable {
	private static final long serialVersionUID = -7272525145274770518L;
	
	@XmlElement(required = true)
	private Long id;
	
	@XmlElement(required = true)
	private String version;
	
	@XmlElement(required = false, nillable=true)
	private String alteration;

	public CertificationResultTestData() {
		super();
	}
	
	public CertificationResultTestData(CertificationResultTestDataDTO dto) {
		this.id = dto.getId();
		this.version = dto.getVersion();
		this.alteration = dto.getAlteration();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getAlteration() {
		return alteration;
	}

	public void setAlteration(String alteration) {
		this.alteration = alteration;
	}
}
