package gov.healthit.chpl.validation.surveillance.reviewer;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.util.SurveillanceUtil;

public abstract class ComparisonReviewer {

    public abstract void review(Surveillance existingSurveillance, Surveillance updatedSurveillance);

    public String getRequirementName(SurveillanceRequirement req) {
        return SurveillanceUtil.getRequirementName(req);
    }

    public String getNonconformityTypeName(SurveillanceNonconformity nc) {
        return SurveillanceUtil.getNonconformityTypeName(nc);
    }
}
