package gov.healthit.chpl.certifiedproduct.service.comparator;

import java.util.Comparator;

import gov.healthit.chpl.domain.CertificationStatusEvent;

public class CertificationStatusEventComparator implements Comparator<CertificationStatusEvent> {

    @Override
    public int compare(CertificationStatusEvent o1, CertificationStatusEvent o2) {
        if (o1.getEventDate() == null || o2.getEventDate() == null
                || o1.getEventDate().equals(o2.getEventDate())) {
            return 0;
        }
        if (o1.getEventDate() < o2.getEventDate()) {
            return -1;
        }
        if (o1.getEventDate() > o2.getEventDate()) {
            return 1;
        }
        return 0;
    }
}
