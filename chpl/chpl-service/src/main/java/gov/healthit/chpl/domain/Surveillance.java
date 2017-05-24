package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlType(namespace = "http://chpl.healthit.gov/listings")
@XmlAccessorType(XmlAccessType.FIELD)
public class Surveillance implements Serializable {
	private static final long serialVersionUID = 7018071250912371691L;
	
	/**
	 * Surveillance internal ID
	 */
	@XmlElement(required = true)
	private Long id;
	
	@XmlTransient
	private String surveillanceIdToReplace;
	
	/**
	 * The user-friendly ID of this surveillance relative to a listing. Ex: SURV01
	 */
	@XmlElement(required = true)
	private String friendlyId;
	
	/**
	 * The listing under surveillance
	 */
	@XmlElement(required = true)
	private CertifiedProduct certifiedProduct;
	
	/**
	 * Date surveillance began
	 */
	@XmlElement(required = true)
	private Date startDate;
	
	/**
	 * Date surveillance ended
	 */
	@XmlElement(required = false, nillable=true)
	private Date endDate;
	
	/**
	 * Type of surveillance (randomized or reactive)
	 */
	@XmlElement(required = true)
	private SurveillanceType type;
	
	/**
	 * Number of randomized sites used. Only applicable for randomized surveillance.
	 */
	@XmlElement(required = false, nillable=true)
	private Integer randomizedSitesUsed;
	
	/**
	 * Surveilled requirements
	 */
	@XmlElementWrapper(name = "surveilledRequirements", nillable = true, required = false)
	@XmlElement(name = "requirement")
	private Set<SurveillanceRequirement> requirements;
	
	@XmlTransient
	private String authority;
	
	@XmlTransient
	private Set<String> errorMessages;
	
	public Surveillance() {
		this.requirements = new LinkedHashSet<SurveillanceRequirement>();
		this.errorMessages = new HashSet<String>();
	}

	public Set<String> getErrorMessages() {
		return errorMessages;
	}

	public void setErrorMessages(Set<String> errors) {
		this.errorMessages = errors;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public CertifiedProduct getCertifiedProduct() {
		return certifiedProduct;
	}

	public void setCertifiedProduct(CertifiedProduct certifiedProduct) {
		this.certifiedProduct = certifiedProduct;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public SurveillanceType getType() {
		return type;
	}

	public void setType(SurveillanceType type) {
		this.type = type;
	}

	public Integer getRandomizedSitesUsed() {
		return randomizedSitesUsed;
	}

	public void setRandomizedSitesUsed(Integer randomizedSitesUsed) {
		this.randomizedSitesUsed = randomizedSitesUsed;
	}

	public Set<SurveillanceRequirement> getRequirements() {
		return requirements;
	}

	public void setRequirements(Set<SurveillanceRequirement> requirements) {
		this.requirements = requirements;
	}

	public String getSurveillanceIdToReplace() {
		return surveillanceIdToReplace;
	}

	public void setSurveillanceIdToReplace(String surveillanceIdToReplace) {
		this.surveillanceIdToReplace = surveillanceIdToReplace;
	}

	public String getFriendlyId() {
		return friendlyId;
	}

	public void setFriendlyId(String friendlyId) {
		this.friendlyId = friendlyId;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

}
