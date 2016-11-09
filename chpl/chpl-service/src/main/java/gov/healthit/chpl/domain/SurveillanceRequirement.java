package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

public class SurveillanceRequirement {
	private Long id;
	private SurveillanceRequirementType type;
	private String requirement;
	private SurveillanceResultType result;
	private List<SurveillanceNonconformity> nonconformities;
	
	public SurveillanceRequirement() {
		this.nonconformities = new ArrayList<SurveillanceNonconformity>();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SurveillanceRequirementType getType() {
		return type;
	}

	public void setType(SurveillanceRequirementType type) {
		this.type = type;
	}

	public String getRequirement() {
		return requirement;
	}

	public void setRequirement(String requirement) {
		this.requirement = requirement;
	}

	public SurveillanceResultType getResult() {
		return result;
	}

	public void setResult(SurveillanceResultType result) {
		this.result = result;
	}

	public List<SurveillanceNonconformity> getNonconformities() {
		return nonconformities;
	}

	public void setNonconformities(List<SurveillanceNonconformity> nonconformities) {
		this.nonconformities = nonconformities;
	}
	
}
