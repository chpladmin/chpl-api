package gov.healthit.chpl.domain;

import gov.healthit.chpl.dto.ProductVersionDTO;

public class ProductVersion {
	private Long versionId;
	private String version;
	private String details;
	private String lastModifiedDate;
	
	public ProductVersion() {}
	
	public ProductVersion(ProductVersionDTO dto) {
		this.versionId = dto.getId();
		this.version = dto.getVersion();
		if(dto.getLastModifiedDate() != null) {
			this.lastModifiedDate = dto.getLastModifiedDate().getTime() + "";
		}
	}

	public Long getVersionId() {
		return versionId;
	}

	public void setVersionId(Long versionId) {
		this.versionId = versionId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
}
