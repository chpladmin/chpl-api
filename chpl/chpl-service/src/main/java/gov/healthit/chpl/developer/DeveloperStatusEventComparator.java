package gov.healthit.chpl.developer;

import java.util.Comparator;

import gov.healthit.chpl.domain.DeveloperStatusEvent;

public class DeveloperStatusEventComparator implements Comparator<DeveloperStatusEvent> {

    @Override
    public int compare(DeveloperStatusEvent o1, DeveloperStatusEvent o2) {
        if (o1.getStatusDate() == null || o2.getStatusDate() == null
                || o1.getStatusDate().equals(o2.getStatusDate())) {
            return 0;
        }
        if (o1.getStatusDate().getTime() < o2.getStatusDate().getTime()) {
            return -1;
        }
        if (o1.getStatusDate().getTime() > o2.getStatusDate().getTime()) {
            return 1;
        }
        return 0;
    }

}
