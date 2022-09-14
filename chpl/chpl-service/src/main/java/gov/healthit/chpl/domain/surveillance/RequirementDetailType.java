package gov.healthit.chpl.domain.surveillance;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.util.Util;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequirementDetailType {
    private Long id;
    private String number;
    private String title;
    private Boolean removed;
    private CertificationEdition certificationEdition;
    private SurveillanceRequirementType surveillanceRequirementType;

    public String getFormattedTitle() {
        if (StringUtils.isNotEmpty(number)) {
            return Util.formatCriteriaNumber(this);
        } else {
            return title;
        }
    }
}
