package gov.healthit.chpl.developer;

import java.util.Comparator;

import gov.healthit.chpl.domain.DeveloperStatusEvent;

public class DeveloperStatusEventComparator implements Comparator<DeveloperStatusEvent> {

    @Override
    public int compare(DeveloperStatusEvent o1, DeveloperStatusEvent o2) {
        if (o1.getStartDay() == null || o2.getStartDay() == null
                || o1.getStartDay().equals(o2.getStartDay())) {
            return 0;
        }
        if (o1.getStartDay().isBefore(o2.getStartDay())) {
            return -1;
        }
        if (o1.getStartDay().isAfter(o2.getStartDay())) {
            return 1;
        }
        return 0;
    }

}
