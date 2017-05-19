package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResultAdditionalSoftware implements Serializable {
	private static final long serialVersionUID = -4131156681875211987L;
	
	@XmlElement(required = true)
	private Long id;
	
	@XmlElement(required = false, nillable=true)
	private String name;
	
	@XmlElement(required = false, nillable=true)
	private String version;
	
	@XmlElement(required = false, nillable=true)
	private Long certifiedProductId;
	
	@XmlElement(required = false, nillable=true)
	private String certifiedProductNumber;
	
	@XmlElement(required = false, nillable=true)
	private String justification;
	
	@XmlElement(required = false, nillable=true)
	private Long certificationResultId;
	
	@XmlElement(required = false, nillable=true)
	private String grouping;
	
	public CertificationResultAdditionalSoftware() {
		super();
	}
	
	public CertificationResultAdditionalSoftware(CertificationResultAdditionalSoftwareDTO dto) {
		this.id = dto.getId();
		this.name = dto.getName();
		this.version = dto.getVersion();
		this.certifiedProductId = dto.getCertifiedProductId();
		this.justification = dto.getJustification();
		this.certificationResultId = dto.getCertificationResultId();
		this.certifiedProductNumber = dto.getCertifiedProductNumber();
		this.grouping = dto.getGrouping();
	}
	
	public String getJustification() {
		return justification;
	}

	public void setJustification(String justification) {
		this.justification = justification;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Long getCertifiedProductId() {
		return certifiedProductId;
	}

	public void setCertifiedProductId(Long certifiedProductId) {
		this.certifiedProductId = certifiedProductId;
	}

	public Long getCertificationResultId() {
		return certificationResultId;
	}

	public void setCertificationResultId(Long certificationResultId) {
		this.certificationResultId = certificationResultId;
	}

	public String getCertifiedProductNumber() {
		return certifiedProductNumber;
	}

	public void setCertifiedProductNumber(String certifiedProductNumber) {
		this.certifiedProductNumber = certifiedProductNumber;
	}

	public String getGrouping() {
		return grouping;
	}

	public void setGrouping(String grouping) {
		this.grouping = grouping;
	}

}
