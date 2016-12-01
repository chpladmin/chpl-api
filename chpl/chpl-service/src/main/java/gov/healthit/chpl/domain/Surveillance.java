package gov.healthit.chpl.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class Surveillance {
	private Long id;
	private String surveillanceIdToReplace;
	private String friendlyId;
	private CertifiedProduct certifiedProduct;
	private Date startDate;
	private Date endDate;
	private SurveillanceType type;
	private Integer randomizedSitesUsed;
	private Set<SurveillanceRequirement> requirements;
	
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
}
