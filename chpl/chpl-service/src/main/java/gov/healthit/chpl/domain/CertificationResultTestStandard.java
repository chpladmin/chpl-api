package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResultTestStandard implements Serializable {
	private static final long serialVersionUID = -9182555768595891414L;
	
	@XmlElement(required = true)
	private Long id;
	
	@XmlElement(required = true)
	private Long testStandardId;
	
	@XmlElement(required = false, nillable=true)
	private String testStandardDescription;
	
	@XmlElement(required = true)
	private String testStandardName;

	public CertificationResultTestStandard() {
		super();
	}
	
	public CertificationResultTestStandard(CertificationResultTestStandardDTO dto) {
		this.id = dto.getId();
		this.testStandardId = dto.getTestStandardId();
		this.testStandardDescription = dto.getTestStandardDescription();
		this.testStandardName = dto.getTestStandardName();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTestStandardId() {
		return testStandardId;
	}

	public void setTestStandardId(Long testStandardId) {
		this.testStandardId = testStandardId;
	}

	public String getTestStandardDescription() {
		return testStandardDescription;
	}

	public void setTestStandardDescription(String testStandardDescription) {
		this.testStandardDescription = testStandardDescription;
	}

	public String getTestStandardName() {
		return testStandardName;
	}

	public void setTestStandardName(String testStandardName) {
		this.testStandardName = testStandardName;
	}

}
