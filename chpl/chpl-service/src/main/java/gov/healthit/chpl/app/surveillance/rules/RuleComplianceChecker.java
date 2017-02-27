package gov.healthit.chpl.app.surveillance.rules;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceOversightRule;

import java.util.Date;

public interface RuleComplianceChecker {	
	public SurveillanceOversightRule getRuleChecked();
	public Date check(CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc);
}
