package gov.healthit.chpl.domain.surveillance;

import gov.healthit.chpl.domain.CertificationEdition;
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
}
