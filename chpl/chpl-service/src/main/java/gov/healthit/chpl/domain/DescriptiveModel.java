package gov.healthit.chpl.domain;

public class DescriptiveModel extends KeyValueModel {
	private String title;
	
	public DescriptiveModel() {
		super();
	}
	
	public DescriptiveModel(Long id, String name, String title) {
		super(id, name);
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
