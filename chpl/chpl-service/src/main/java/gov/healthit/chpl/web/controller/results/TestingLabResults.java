package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.TestingLab;

public class TestingLabResults {
	private List<TestingLab> atls;

	public TestingLabResults() {
		atls = new ArrayList<TestingLab>();
	}

	public List<TestingLab> getAtls() {
		return atls;
	}

	public void setAtls(List<TestingLab> atls) {
		this.atls = atls;
	}
}
