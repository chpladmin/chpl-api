package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.MeaningfulUseUser;

public class MeaningfulUseUserResults implements Serializable {
	private static final long serialVersionUID = 4865758231142816185L;
	private List<MeaningfulUseUser> results;
	private List<MeaningfulUseUser> errors;

	public MeaningfulUseUserResults() {
		results = new ArrayList<MeaningfulUseUser>();
		errors = new ArrayList<MeaningfulUseUser>();
	}
	
	public MeaningfulUseUserResults(List<MeaningfulUseUser> results) {
		this.results = results;
	}

	public List<MeaningfulUseUser> getResults() {
		return results;
	}

	public void setResults(List<MeaningfulUseUser> results) {
		this.results = results;
	}

	public List<MeaningfulUseUser> getErrors() {
		return errors;
	}

	public void setErrors(List<MeaningfulUseUser> errors) {
		this.errors = errors;
	}
}
