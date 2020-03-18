package gov.healthit.chpl.util;

import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;

public class SurveillanceUtil {

    public static String getRequirementName(SurveillanceRequirement req) {
        if (req.getCriterion() == null) {
            return req.getRequirement();
        }
        return Util.formatCriteriaNumber(req.getCriterion());
    }

    public static String getNonconformityTypeName(SurveillanceNonconformity nc) {
        if (nc.getCriterion() == null) {
            return nc.getNonconformityType();
        }
        return Util.formatCriteriaNumber(nc.getCriterion());
    }
}
