package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.Announcement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnnouncementResults implements Serializable {
    private static final long serialVersionUID = 8748247550112564530L;
    private List<Announcement> announcements;

    public AnnouncementResults() {
        announcements = new ArrayList<Announcement>();
    }
}
