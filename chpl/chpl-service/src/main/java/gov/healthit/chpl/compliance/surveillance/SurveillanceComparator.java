package gov.healthit.chpl.compliance.surveillance;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SurveillanceComparator implements Comparator<Surveillance> {

    @Override
    public int compare(Surveillance surv1, Surveillance surv2) {
        if (!StringUtils.isEmpty(surv1.getFriendlyId()) && !StringUtils.isEmpty(surv2.getFriendlyId())) {
            return surv1.getFriendlyId().compareTo(surv2.getFriendlyId());
        } else if (surv1.getId() != null && surv2.getId() != null) {
            return surv1.getId().compareTo(surv2.getId());
        }
        return 0;
    }
}
