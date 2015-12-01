package gov.healthit.chpl.domain;

import java.util.List;

import gov.healthit.chpl.auth.json.User;

public class UserActivity {
	
	private User user;
	private List<ActivityEvent> events;
	
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public List<ActivityEvent> getEvents() {
		return events;
	}
	public void setEvents(List<ActivityEvent> events) {
		this.events = events;
	}
}
