package gov.healthit.chpl.domain;

import java.util.Date;

public class ApiKeyActivity {
	
	private Long id;
	private Long apiKeyId;
	private String apiKey;
	private String email;
	private String name;
	private String apiCallPath;
	private Date creationDate;
	
	public ApiKeyActivity(){}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getApiKeyId() {
		return apiKeyId;
	}
	public void setApiKeyId(Long apiKeyId) {
		this.apiKeyId = apiKeyId;
	}
	public String getApiCallPath() {
		return apiCallPath;
	}
	public void setApiCallPath(String apiCallPath) {
		this.apiCallPath = apiCallPath;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
