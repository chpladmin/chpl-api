package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.Surveillance;

public class SurveillanceResults {
	private List<Surveillance> pendingSurveillance;

	public SurveillanceResults() {
		pendingSurveillance = new ArrayList<Surveillance>();
	}

	public List<Surveillance> getPendingSurveillance() {
		return pendingSurveillance;
	}

	public void setPendingSurveillance(List<Surveillance> pendingSurveillance) {
		this.pendingSurveillance = pendingSurveillance;
	}

}
