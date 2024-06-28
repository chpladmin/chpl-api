package gov.healthit.chpl.developer;

import java.util.Comparator;

import gov.healthit.chpl.domain.DeveloperStatusEvent;

public class DeveloperStatusEventComparator implements Comparator<DeveloperStatusEvent> {

    @Override
    public int compare(DeveloperStatusEvent o1, DeveloperStatusEvent o2) {
        if (o1.getStartDate() == null || o2.getStartDate() == null
                || o1.getStartDate().equals(o2.getStartDate())) {
            return 0;
        }
        if (o1.getStartDate().isBefore(o2.getStartDate())) {
            return -1;
        }
        if (o1.getStartDate().isAfter(o2.getStartDate())) {
            return 1;
        }
        return 0;
    }

}
