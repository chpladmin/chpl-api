package gov.healthit.chpl.optionalStandard;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class OptionalStandardComparator implements Comparator<OptionalStandard> {

    @Override
    public int compare(OptionalStandard os1, OptionalStandard os2) {
        if (!StringUtils.isEmpty(os1.getCitation()) && !StringUtils.isEmpty(os2.getCitation())) {
            return os1.getCitation().compareTo(os2.getCitation());
        } else if (os1.getId() != null && os2.getId() != null) {
            return os1.getId().compareTo(os2.getId());
        }
        return 0;
    }
}
