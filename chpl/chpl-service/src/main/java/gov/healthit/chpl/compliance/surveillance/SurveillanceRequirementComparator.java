package gov.healthit.chpl.compliance.surveillance;

import java.util.Comparator;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SurveillanceRequirementComparator implements Comparator<SurveillanceRequirement> {

    @Override
    public int compare(SurveillanceRequirement req1, SurveillanceRequirement req2) {
        if (ObjectUtils.allNotNull(req1.getRequirementType(), req2.getRequirementType())
                && !StringUtils.isEmpty(req1.getRequirementType().getFormattedTitle())
                && !StringUtils.isEmpty(req2.getRequirementType().getFormattedTitle())) {
            return req1.getRequirementType().getFormattedTitle().compareTo(
                    req2.getRequirementType().getFormattedTitle());
        } else if (req1.getId() != null && req2.getId() != null) {
            return req1.getId().compareTo(req2.getId());
        }
        return 0;
    }
}
