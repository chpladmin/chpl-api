package gov.healthit.chpl.web.controller;

import java.util.List;

public class CertificationIdVerificationBody {

	private List<String> ids;

	public CertificationIdVerificationBody() {}

	public CertificationIdVerificationBody(List<String> ids) {
		this.ids = ids;
	}
	public List<String> getIds() {
		return this.ids;
	}
	public void setIds(List<String> ids) {
		this.ids = ids;
	}

}
