package gov.healthit.chpl.app.surveillance.rules;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;

import java.util.Date;

public abstract class NonconformityRuleComplianceChecker implements RuleChecker {	
	public abstract Date check(Surveillance surv, SurveillanceNonconformity nc);
}
