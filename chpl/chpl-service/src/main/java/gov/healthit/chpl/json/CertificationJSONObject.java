package gov.healthit.chpl.json;

public class CertificationJSONObject {
	
	
	private String number;
	private String title;
	private boolean hasVersion;
	private boolean isActive;
	private String version;
	
	
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public boolean isHasVersion() {
		return hasVersion;
	}
	public void setHasVersion(boolean hasVersion) {
		this.hasVersion = hasVersion;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
}
