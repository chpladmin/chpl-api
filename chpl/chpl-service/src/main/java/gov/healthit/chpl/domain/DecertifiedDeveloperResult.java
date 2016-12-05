package gov.healthit.chpl.domain;

import java.util.List;

import gov.healthit.chpl.dto.DeveloperDTO;

public class DecertifiedDeveloperResult {

	private Developer developer;
	private List<CertificationBody> certifyingBody;
	private Long estimatedUsers;
	
	public DecertifiedDeveloperResult(){}
	
	public DecertifiedDeveloperResult(Developer developer, List<CertificationBody> certifyingBody, Long estimatedUsers){
		this.developer = developer;
		this.certifyingBody = certifyingBody; 
		this.estimatedUsers = estimatedUsers;
	}
	
	public DecertifiedDeveloperResult(DeveloperDTO developerDTO, List<CertificationBody> certifyingBody, Long estimatedUsers){
		this.developer = new Developer(developerDTO);
		this.certifyingBody = certifyingBody; 
		this.estimatedUsers = estimatedUsers;
	}

	public Developer getDeveloper() {
		return developer;
	}

	public void setDeveloper(Developer developer) {
		this.developer = developer;
	}

	public List<CertificationBody> getCertifyingBody() {
		return certifyingBody;
	}

	public void setCertifyingBody(List<CertificationBody> certifyingBody) {
		this.certifyingBody = certifyingBody;
	}

	public Long getEstimatedUsers() {
		return estimatedUsers;
	}

	public void setEstimatedUsers(Long estimatedUsers) {
		this.estimatedUsers = estimatedUsers;
	};
}
