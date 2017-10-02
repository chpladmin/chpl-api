package gov.healthit.chpl.domain;

import java.io.Serializable;

public class UpdateVersionRequest implements Serializable {
	private static final long serialVersionUID = 7332486411279338020L;
	private Long versionId;
	private String version;

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


}
