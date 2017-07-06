package gov.healthit.chpl.domain;

import java.io.Serializable;

public class DeveloperTransparency implements Serializable {
	private static final long serialVersionUID = -5492650176812222242L;
	
	private Long id;
	private String name;
	private String status;
	private String transparencyAttestationUrls;
	private String acbAttestations;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTransparencyAttestationUrls() {
		return transparencyAttestationUrls;
	}
	public void setTransparencyAttestationUrls(String transparencyAttestationUrls) {
		this.transparencyAttestationUrls = transparencyAttestationUrls;
	}
	public String getAcbAttestations() {
		return acbAttestations;
	}
	public void setAcbAttestations(String acbAttestations) {
		this.acbAttestations = acbAttestations;
	}
}
