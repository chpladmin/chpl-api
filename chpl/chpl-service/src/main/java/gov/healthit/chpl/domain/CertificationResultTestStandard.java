package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;

/**
 * A standard used to meet a certification criterion.
 * You can find a list of potential values in the 2014 or 2015 Functionality and Standards Reference Tables.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResultTestStandard implements Serializable {
	private static final long serialVersionUID = -9182555768595891414L;
	
	/**
	 * Test standard to certification result mapping internal ID
	 */
	@XmlElement(required = true)
	private Long id;
	
	/**
	 * Test standard internal ID
	 */
	@XmlElement(required = true)
	private Long testStandardId;
	
	/**
	 * Description of test standard
	 */
	@XmlElement(required = false, nillable=true)
	private String testStandardDescription;
	
	/**
	 * Name of test standard
	 */
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
