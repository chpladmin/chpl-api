package gov.healthit.chpl.domain;

public class ProductActivityEvent extends ActivityEvent {
	private Developer developer;

	public Developer getDeveloper() {
		return developer;
	}

	public void setDeveloper(Developer developer) {
		this.developer = developer;
	}
	
}
