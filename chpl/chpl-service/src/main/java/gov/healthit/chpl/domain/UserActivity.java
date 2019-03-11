package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.domain.activity.ActivityEvent;

public class UserActivity implements Serializable {
    private static final long serialVersionUID = -4162353900589961524L;
    private User user;
    private List<ActivityEvent> events;

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public List<ActivityEvent> getEvents() {
        return events;
    }

    public void setEvents(final List<ActivityEvent> events) {
        this.events = events;
    }
}
