package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SearchOption implements Serializable {
	private static final long serialVersionUID = -6671338026335670632L;
	private boolean expandable;
	private Set<? extends KeyValueModel> data;
	
	public SearchOption() {
		this.data = new HashSet<KeyValueModel>();
	}

	public boolean isExpandable() {
		return expandable;
	}

	public void setExpandable(boolean expandable) {
		this.expandable = expandable;
	}

	public Set<? extends KeyValueModel> getData() {
		return data;
	}

	public void setData(Set<? extends KeyValueModel> data) {
		this.data = data;
	}
	
}
