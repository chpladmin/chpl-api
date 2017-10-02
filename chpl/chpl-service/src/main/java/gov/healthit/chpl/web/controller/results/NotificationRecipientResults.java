package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.notification.Recipient;

public class NotificationRecipientResults implements Serializable {
    private static final long serialVersionUID = 8748244450112564530L;
    private List<Recipient> results;

    public NotificationRecipientResults() {
        results = new ArrayList<Recipient>();
    }

    public List<Recipient> getResults() {
        return results;
    }

    public void setResults(List<Recipient> results) {
        this.results = results;
    }
}
