package gov.healthit.chpl.domain.comparator;

import java.util.Comparator;

import gov.healthit.chpl.domain.CertificationStatusEvent;

public class CertificationStatusEventComparator implements Comparator<CertificationStatusEvent> {

    @Override
    public int compare(CertificationStatusEvent o1, CertificationStatusEvent o2) {
        if (o1.getEventDay() == null || o2.getEventDay() == null
                || o1.getEventDay().equals(o2.getEventDay())) {
            return 0;
        }
        return o1.getEventDay().compareTo(o2.getEventDay());
    }
}
