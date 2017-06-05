package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CertifiedProductQmsStandardDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertifiedProductQmsStandard implements Serializable {
	private static final long serialVersionUID = -2085183878828053974L;
	
	@XmlElement(required = true)
	private Long id;
	
	@XmlElement(required = true)
	private Long qmsStandardId;
	
	@XmlElement(required = false, nillable=true)
	private String qmsStandardName;
	
	@XmlElement(required = false, nillable=true)
	private String qmsModification;
	
	@XmlElement(required = false, nillable=true)
	private String applicableCriteria;

	public CertifiedProductQmsStandard() {
		super();
	}
	
	public CertifiedProductQmsStandard(CertifiedProductQmsStandardDTO dto) {
		this.id = dto.getId();
		this.qmsStandardId = dto.getQmsStandardId();
		this.qmsStandardName = dto.getQmsStandardName();
		this.qmsModification = dto.getQmsModification();
		this.applicableCriteria = dto.getApplicableCriteria();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getQmsStandardId() {
		return qmsStandardId;
	}

	public void setQmsStandardId(Long qmsStandardId) {
		this.qmsStandardId = qmsStandardId;
	}

	public String getQmsStandardName() {
		return qmsStandardName;
	}

	public void setQmsStandardName(String qmsStandardName) {
		this.qmsStandardName = qmsStandardName;
	}

	public String getQmsModification() {
		return qmsModification;
	}

	public void setQmsModification(String qmsModification) {
		this.qmsModification = qmsModification;
	}

	public String getApplicableCriteria() {
		return applicableCriteria;
	}

	public void setApplicableCriteria(String applicableCriteria) {
		this.applicableCriteria = applicableCriteria;
	}
}
