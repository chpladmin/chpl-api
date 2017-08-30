package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.domain.Developer;

public class DeveloperResults implements Serializable {
	private static final long serialVersionUID = -35459612531309700L;
	private List<Developer> developers;

	public List<Developer> getDevelopers() {
		return developers;
	}

	public void setDevelopers(List<Developer> developers) {
		this.developers = developers;
	}
}
