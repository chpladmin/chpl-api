package gov.healthit.chpl.compliance.surveillance;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SurveillanceNonconformityComparator implements Comparator<SurveillanceNonconformity> {

    @Override
    public int compare(SurveillanceNonconformity nc1, SurveillanceNonconformity nc2) {
        if (ObjectUtils.allNotNull(nc1.getType(), nc2.getType())
                && !StringUtils.isEmpty(nc1.getType().getFormattedTitle())
                && !StringUtils.isEmpty(nc2.getType().getFormattedTitle())) {
            return nc1.getType().getFormattedTitle().compareTo(
                    nc2.getType().getFormattedTitle());
        } else if (nc1.getId() != null && nc2.getId() != null) {
            return nc1.getId().compareTo(nc2.getId());
        }
        return 0;
    }
}
