package gov.healthit.chpl.domain;

import java.util.ArrayList;
import java.util.List;

public class TabularValues {
	private String name;
	private List<String> headings;
	private List<List<String>> values;
	
	public TabularValues() {
		headings = new ArrayList<String>();
		values = new ArrayList<List<String>>();
	}

	public List<String> getHeadings() {
		return headings;
	}

	public void setHeadings(List<String> headings) {
		this.headings = headings;
	}

	public List<List<String>> getValues() {
		return values;
	}

	public void setValues(List<List<String>> values) {
		this.values = values;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
