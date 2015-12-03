package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

public class SurveillanceResults {
	private List<SurveillanceDetails> surveillances;
	
	public SurveillanceResults() {
		surveillances = new ArrayList<SurveillanceDetails>();
	}

	public List<SurveillanceDetails> getSurveillances() {
		return surveillances;
	}

	public void setSurveillances(List<SurveillanceDetails> surveillances) {
		this.surveillances = surveillances;
	}

}
