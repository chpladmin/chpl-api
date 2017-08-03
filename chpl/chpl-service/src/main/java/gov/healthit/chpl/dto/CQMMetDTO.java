package gov.healthit.chpl.dto;

import java.io.Serializable;

public class CQMMetDTO implements Serializable {
	private static final long serialVersionUID = -2966774346743452242L;
	private String cmsId;
	private String version;
	private String domain;
	
	public CQMMetDTO(){}
	
	public CQMMetDTO(String cmsId, String version, String domain) {
		this.cmsId = cmsId;
		this.version = version;
		this.domain = domain;
	}

	public void setCmsId(String cmsId) {
		this.cmsId = cmsId;
	}
	
	public String getCmsId() {
		return this.cmsId;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getVersion() {
		return this.version;
	}
	
	public void setDomain(String domain) {
		this.domain = domain;
	}
	
	public String getDomain() {
		return this.domain;
	}
	
}
