package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.MeaningfulUseUser;

public class MeaningfulUseUserResults {
	private List<MeaningfulUseUser> meaningfulUseUsers;

	public MeaningfulUseUserResults() {
		meaningfulUseUsers = new ArrayList<MeaningfulUseUser>();
	}
	
	public MeaningfulUseUserResults(List<MeaningfulUseUser> meaningfulUseUsers) {
		this.meaningfulUseUsers = meaningfulUseUsers;
	}

	public List<MeaningfulUseUser> getMeaningfulUseUsers() {
		return meaningfulUseUsers;
	}

	public void setMeaningfulUseUsers(List<MeaningfulUseUser> meaningfulUseUsers) {
		this.meaningfulUseUsers = meaningfulUseUsers;
	}
}
