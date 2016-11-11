package gov.healthit.chpl.dao;

import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;

public interface SurveillanceDAO {
	public SurveillanceType findSurveillanceType(String type);
	public SurveillanceRequirementType findSurveillanceRequirementType(String type);
	public SurveillanceResultType findSurveillanceResultType(String type);
	public SurveillanceNonconformityStatus findSurveillanceNonconformityStatusType(String type);
}
