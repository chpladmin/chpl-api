package gov.healthit.chpl.domain;

import java.util.HashSet;
import java.util.Set;

public class SearchOption {
	private boolean expandable;
	private Set<KeyValueModel> data;
	
	public SearchOption() {
		this.data = new HashSet<KeyValueModel>();
	}

	public boolean isExpandable() {
		return expandable;
	}

	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}

	public Set<KeyValueModel> getData() {
		return data;
	}

	public void setData(Set<KeyValueModel> data) {
		this.data = data;
	}
	
}
