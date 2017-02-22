package gov.healthit.chpl.app.surveillance.rules;

import java.util.Date;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;

public abstract class SurveillanceRuleComplianceChecker implements RuleChecker {	
	public abstract Date check(CertifiedProductSearchDetails cp, Surveillance surv);
}
