package gov.healthit.chpl.developer;

import java.util.Comparator;

import gov.healthit.chpl.domain.DeveloperStatusEventDeprecated;

@Deprecated
public class DeveloperStatusEventComparatorDeprecated implements Comparator<DeveloperStatusEventDeprecated> {

    @Override
    public int compare(DeveloperStatusEventDeprecated o1, DeveloperStatusEventDeprecated o2) {
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
