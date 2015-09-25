package gov.healthit.chpl.domain;

import java.util.List;

public class UpdateVersionsRequest {
	private List<Long> versionIds;
	private ProductVersion version;
	private Long newProductId;
	
	public List<Long> getVersionIds() {
		return versionIds;
	}
	public void setVersionIds(List<Long> versionIds) {
		this.versionIds = versionIds;
	}
	public ProductVersion getVersion() {
		return version;
	}
	public void setVersion(ProductVersion version) {
		this.version = version;
	}
	public Long getNewProductId() {
		return newProductId;
	}
	public void setNewProductId(Long newProductId) {
		this.newProductId = newProductId;
	}
	
}
