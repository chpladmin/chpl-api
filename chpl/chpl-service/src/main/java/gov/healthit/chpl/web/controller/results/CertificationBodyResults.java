package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CertificationBody;

public class CertificationBodyResults {
	private List<CertificationBody> acbs;

	public CertificationBodyResults() {
		acbs = new ArrayList<CertificationBody>();
	}
	
	public List<CertificationBody> getAcbs() {
		return acbs;
	}

	public void setAcbs(List<CertificationBody> acbs) {
		this.acbs = acbs;
	}

	
}
