package gov.healthit.chpl.domain;

import java.util.List;

import gov.healthit.chpl.dto.DeveloperDecertifiedDTO;

public class DeveloperDecertificationResponse {
	
	private List<DeveloperDecertifiedDTO> developerDecertificationResult;
	
	public DeveloperDecertificationResponse(){}

	public List<DeveloperDecertifiedDTO> getDeveloperDecertificationResult() {
		return developerDecertificationResult;
	}

	public void setDeveloperDecertificationResult(List<DeveloperDecertifiedDTO> developerDecertificationResult) {
		this.developerDecertificationResult = developerDecertificationResult;
	};

}
