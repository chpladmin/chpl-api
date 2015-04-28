package gov.healthit.chpl;

import java.util.ArrayList;
import java.util.List;

public class CertificationBody {
	
	private List<String> permissions = new ArrayList<String>();

	public List<String> getPermissions() {
		return permissions;
	}

	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}

}
