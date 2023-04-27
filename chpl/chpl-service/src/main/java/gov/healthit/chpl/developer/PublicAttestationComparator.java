package gov.healthit.chpl.developer;

import java.util.Comparator;

import gov.healthit.chpl.domain.PublicAttestation;

public class PublicAttestationComparator implements Comparator<PublicAttestation> {

    @Override
    public int compare(PublicAttestation o1, PublicAttestation o2) {
        if (o1.getAttestationPeriod() == null || o2.getAttestationPeriod() == null
                || o1.getAttestationPeriod().getPeriodStart().equals(o2.getAttestationPeriod().getPeriodStart())) {
            return 0;
        }
        return o1.getAttestationPeriod().getPeriodStart().compareTo(o2.getAttestationPeriod().getPeriodStart());
    }

}
