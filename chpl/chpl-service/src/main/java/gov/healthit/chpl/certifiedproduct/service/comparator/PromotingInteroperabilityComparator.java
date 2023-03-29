package gov.healthit.chpl.certifiedproduct.service.comparator;

import java.util.Comparator;

import gov.healthit.chpl.domain.PromotingInteroperabilityUser;

public class PromotingInteroperabilityComparator implements Comparator<PromotingInteroperabilityUser> {

    @Override
    public int compare(PromotingInteroperabilityUser pi1, PromotingInteroperabilityUser pi2) {
        if (pi1.getUserCountDate() == null || pi2.getUserCountDate() == null
                || pi1.getUserCountDate().equals(pi2.getUserCountDate())) {
            return 0;
        }
        if (pi1.getUserCountDate().isBefore(pi2.getUserCountDate())) {
            return -1;
        }
        if (pi1.getUserCountDate().isAfter(pi2.getUserCountDate())) {
            return 1;
        }
        return 0;
    }
}
