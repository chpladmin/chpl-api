package gov.healthit.chpl.web.controller.results;

import java.util.List;

import gov.healthit.chpl.domain.DecertifiedDeveloperResult;

public class DecertifiedDeveloperResults {
	private List<DecertifiedDeveloperResult> decertifiedDeveloperResults;
	
	public DecertifiedDeveloperResults(){}

	public List<DecertifiedDeveloperResult> getDecertifiedDeveloperResults() {
		return decertifiedDeveloperResults;
	}

	public void setDecertifiedDeveloperResults(List<DecertifiedDeveloperResult> decertifiedDeveloperResults) {
		this.decertifiedDeveloperResults = decertifiedDeveloperResults;
	};
	
}
