package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import gov.healthit.chpl.dto.CertificationResultTestToolDTO;

/**
 * The test tool used to certify the Health IT Module to the corresponding certification criteria
 * Allowable values are based on the NIST 2014 and 2015 Edition Test Tools.
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResultTestTool implements Serializable {
	private static final long serialVersionUID = 2785949879671019720L;
	
	/**
	 * Test tool to certification result mapping internal ID
	 */
	@XmlElement(required = true)
	private Long id;
	
	/**
	 * Test tool internal ID
	 */
	@XmlElement(required = true)
	private Long testToolId;
	
	/**
	 * The test tool used to certify the Health IT Module to the corresponding certification criteria
	 */
	@XmlElement(required = true)
	private String testToolName;
	
	/**
	 * The version of the test tool being used.
	 */
	@XmlElement(required = false, nillable=true)
	private String testToolVersion;
	
	/**
	 * Whether or not the test tool has been retired.
	 */
	@XmlElement(required = false, nillable=true)
	private boolean retired;
	
	public CertificationResultTestTool() {
		super();
	}
	
	public CertificationResultTestTool(CertificationResultTestToolDTO dto) {
		this.id = dto.getId();
		this.testToolId = dto.getTestToolId();
		this.testToolName = dto.getTestToolName();
		this.testToolVersion = dto.getTestToolVersion();
		this.retired = dto.isRetired();
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTestToolId() {
		return testToolId;
	}

	public void setTestToolId(Long testToolId) {
		this.testToolId = testToolId;
	}

	public String getTestToolName() {
		return testToolName;
	}

	public void setTestToolName(String testToolName) {
		this.testToolName = testToolName;
	}

	public String getTestToolVersion() {
		return testToolVersion;
	}

	public void setTestToolVersion(String testToolVersion) {
		this.testToolVersion = testToolVersion;
	}

	public boolean isRetired() {
		return retired;
	}

	public void setRetired(boolean retired) {
		this.retired = retired;
	}
}
