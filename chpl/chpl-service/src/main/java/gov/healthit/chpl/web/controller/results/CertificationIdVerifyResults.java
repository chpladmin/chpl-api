package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CertificationIdVerifyResults implements Serializable {
	private static final long serialVersionUID = -3582436342627660622L;
	List<VerifyResult> results = new ArrayList<VerifyResult>();

	public CertificationIdVerifyResults() {
	}
	
	public CertificationIdVerifyResults(Map<String, Boolean> map) {
		this.importMap(map);
	}
	
	public List<VerifyResult> getResults() {
		return this.results;
	}

	private void importMap(Map<String, Boolean> map) {
		for (String id : map.keySet()) {
			VerifyResult item = new VerifyResult(id, map.get(id));
			this.results.add(item);
		}
	}
	
	static public class VerifyResult {
		private String id;
		private boolean valid;
	
		public VerifyResult(String id, Boolean valid) {
			this.id = id;
			this.valid = valid;
		}
		
		public String getId() {
			return this.id;
		}
		
		public void setId(String id) {
			this.id = id;
		}
		
		public boolean getValid() {
			return this.valid;
		}
		
		public void setValid(boolean valid) {
			this.valid = valid;
		}

	}

}
