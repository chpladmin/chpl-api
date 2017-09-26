package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.domain.Job;

public class JobResults implements Serializable {
	private static final long serialVersionUID = -35459272531309700L;
	private List<Job> results;
	public List<Job> getResults() {
		return results;
	}
	public void setResults(List<Job> results) {
		this.results = results;
	}
}
