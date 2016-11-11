package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.Surveillance;

public class SurveillanceResults {
	private List<Surveillance> results;

	public SurveillanceResults() {
		results = new ArrayList<Surveillance>();
	}

	public List<Surveillance> getResults() {
		return results;
	}

	public void setResults(List<Surveillance> results) {
		this.results = results;
	}
}
