package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.domain.activity.ActivityDetails;

public class UserActivity implements Serializable {
    private static final long serialVersionUID = -4162353900589961524L;
    private User user;
    private List<ActivityDetails> events;

    public User getUser() {
        return user;
    }

    public void setUser(final User user) {
        this.user = user;
    }

    public List<ActivityDetails> getEvents() {
        return events;
    }

    public void setEvents(final List<ActivityDetails> events) {
        this.events = events;
    }
}
