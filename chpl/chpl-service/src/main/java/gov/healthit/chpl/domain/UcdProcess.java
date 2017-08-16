package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;

/**
 * The user-centered design (UCD) process applied for the corresponding certification criteria
 *
 */
@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class UcdProcess implements Serializable {
	private static final long serialVersionUID = 7248865611086710891L;
	
	/**
	 * UCD process to certification result internal mapping ID
	 */
	@XmlElement(required = true)
	private Long id;
	
	/**
	 * UCD process internal ID
	 */
	@XmlElement(required = true)
	private Long ucdProcessId;
	
	/**
	 * The user-centered design (UCD) process applied for the corresponding certification criteria
	 */
	@XmlElement(required = true)
	private String ucdProcessName;
	
	/**
	 * A description of the UCD process used. This variable is applicable 
	 * for 2014 and 2015 Edition, and a string variable that does not take any restrictions 
	 * on formatting or values. 
	 */
	@XmlElement(required = false, nillable=true)
	private String ucdProcessDetails;

	/**
	 * The set of criteria within a listing to which this UCD process is applied.
	 */
	@XmlElement(required = false, nillable = true)
	private Set<CertificationCriterion> criteria;
	
	public UcdProcess() {
		super();
		this.criteria = new HashSet<CertificationCriterion>();
	}
	
	public UcdProcess(CertificationResultUcdProcessDTO dto) {
		this();
		this.id = dto.getId();
		this.ucdProcessId = dto.getUcdProcessId();
		this.ucdProcessName = dto.getUcdProcessName();
		this.ucdProcessDetails = dto.getUcdProcessDetails();
	}
	
	public boolean matches(UcdProcess anotherUcd) {
		boolean result = false;
		if(this.getUcdProcessId() != null && anotherUcd.getUcdProcessId() != null && 
				this.getUcdProcessId().longValue() == anotherUcd.getUcdProcessId().longValue() && 
				((StringUtils.isEmpty(this.getUcdProcessDetails()) && StringUtils.isEmpty(anotherUcd.getUcdProcessDetails())) || 
				this.getUcdProcessDetails().equals(anotherUcd.getUcdProcessDetails()))) {
			result = true;
		} else if(!StringUtils.isEmpty(this.getUcdProcessName()) && !StringUtils.isEmpty(anotherUcd.getUcdProcessName()) && 
				this.getUcdProcessName().equalsIgnoreCase(anotherUcd.getUcdProcessName()) && 
				((StringUtils.isEmpty(this.getUcdProcessDetails()) && StringUtils.isEmpty(anotherUcd.getUcdProcessDetails())) || 
				this.getUcdProcessDetails().equals(anotherUcd.getUcdProcessDetails()))) {
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

	public Long getUcdProcessId() {
		return ucdProcessId;
	}

	public void setUcdProcessId(Long ucdProcessId) {
		this.ucdProcessId = ucdProcessId;
	}

	public String getUcdProcessName() {
		return ucdProcessName;
	}

	public void setUcdProcessName(String ucdProcessName) {
		this.ucdProcessName = ucdProcessName;
	}

	public String getUcdProcessDetails() {
		return ucdProcessDetails;
	}

	public void setUcdProcessDetails(String ucdProcessDetails) {
		this.ucdProcessDetails = ucdProcessDetails;
	}

	public Set<CertificationCriterion> getCriteria() {
		return criteria;
	}

	public void setCriteria(Set<CertificationCriterion> criteria) {
		this.criteria = criteria;
	}
}
