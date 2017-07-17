package gov.healthit.chpl.domain;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;

/**
 * The test procedure used for the certification criteria
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class CertificationResultTestProcedure implements Serializable {
	private static final long serialVersionUID = -8648559250833503194L;
	
	/**
	 * Test Procedure to certification result mapping internal ID
	 */
	@XmlElement(required = true)
	private Long id;
	
	/**
	 * Test procedure internal ID
	 */
	@XmlElement(required = true)
	private Long testProcedureId;
	
	/**
	 * The test procedure version used for a given certification criteria. 
	 * This variable is a string variable that does not take any restrictions on formatting 
	 * or values and is applicable for 2014 and 2015 Edition. 
	 */
	@XmlElement(required = true)
	private String testProcedureVersion;

	public CertificationResultTestProcedure() {
		super();
	}
	
	public CertificationResultTestProcedure(CertificationResultTestProcedureDTO dto) {
		this.id = dto.getId();
		this.testProcedureId = dto.getTestProcedureId();
		this.testProcedureVersion = dto.getTestProcedureVersion();
	}
	
	public boolean matches(CertificationResultTestProcedure anotherProc) {
		boolean result = false;
		if(this.getTestProcedureId() != null && anotherProc.getTestProcedureId() != null && 
				this.getTestProcedureId().longValue() == anotherProc.getTestProcedureId().longValue()) {
			result = true;
		} else if(!StringUtils.isEmpty(this.getTestProcedureVersion()) && 
				!StringUtils.isEmpty(anotherProc.getTestProcedureVersion()) && 
				this.getTestProcedureVersion().equalsIgnoreCase(anotherProc.getTestProcedureVersion())) {
			result = true;
		}
		return result;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getTestProcedureId() {
		return testProcedureId;
	}

	public void setTestProcedureId(Long testProcedureId) {
		this.testProcedureId = testProcedureId;
	}

	public String getTestProcedureVersion() {
		return testProcedureVersion;
	}

	public void setTestProcedureVersion(String testProcedureVersion) {
		this.testProcedureVersion = testProcedureVersion;
	}
}
