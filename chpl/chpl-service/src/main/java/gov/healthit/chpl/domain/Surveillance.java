package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Surveillance {
	private Long id;
	private CertifiedProduct certifiedProduct;
	private Date startDate;
	private Date endDate;
	private SurveillanceType type;
	private Integer randomizedSitesUsed;
	private List<SurveillanceRequirement> requirements;
	
	public Surveillance() {
		this.requirements = new ArrayList<SurveillanceRequirement>();
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

	public List<SurveillanceRequirement> getRequirements() {
		return requirements;
	}

	public void setRequirements(List<SurveillanceRequirement> requirements) {
		this.requirements = requirements;
	}
}
