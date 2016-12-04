package gov.healthit.chpl.domain;

import java.util.List;

import gov.healthit.chpl.dto.DecertifiedDeveloperDTO;

public class DeveloperDecertificationResponse {
	
	private List<DecertifiedDeveloperDTO> developerDecertificationResult;
	
	public DeveloperDecertificationResponse(){}

	public List<DecertifiedDeveloperDTO> getDeveloperDecertificationResult() {
		return developerDecertificationResult;
	}

	public void setDeveloperDecertificationResult(List<DecertifiedDeveloperDTO> developerDecertificationResult) {
		this.developerDecertificationResult = developerDecertificationResult;
	}

}
