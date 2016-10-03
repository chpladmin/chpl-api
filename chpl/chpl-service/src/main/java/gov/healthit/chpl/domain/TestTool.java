package gov.healthit.chpl.domain;

public class TestTool extends KeyValueModel {
	private boolean retired;
	
	public TestTool() {
		super();
	}
	public TestTool(Long id, String name) {
		super(id, name);
	}
	public TestTool(Long id, String name, String description) {
		super(id, name, description);
	}
	
	public boolean getRetired() {
		return retired;
	}
	public void setRetired(boolean retired) {
		this.retired = retired;
	}
	
}
