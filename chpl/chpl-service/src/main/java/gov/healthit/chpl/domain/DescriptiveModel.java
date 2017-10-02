package gov.healthit.chpl.domain;

import java.io.Serializable;

public class DescriptiveModel extends KeyValueModel implements Serializable {
	private static final long serialVersionUID = 1402909764642483654L;
	private String title;

	public DescriptiveModel() {
		super();
	}

	public DescriptiveModel(Long id, String name, String title) {
		super(id, name);
		this.title = title;
	}

	public DescriptiveModel(Long id, String name, String title, String description) {
		super(id, name, description);
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
