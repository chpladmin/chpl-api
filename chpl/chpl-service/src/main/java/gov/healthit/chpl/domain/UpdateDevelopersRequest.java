package gov.healthit.chpl.domain;

import java.util.List;

public class UpdateDevelopersRequest {
	private List<Long> developerIds;
	private Developer developer;
	public List<Long> getDeveloperIds() {
		return developerIds;
	}
	public void setDeveloperIds(List<Long> developerIds) {
		this.developerIds = developerIds;
	}
	public Developer getDeveloper() {
		return developer;
	}
	public void setDeveloper(Developer developer) {
		this.developer = developer;
	}
}
