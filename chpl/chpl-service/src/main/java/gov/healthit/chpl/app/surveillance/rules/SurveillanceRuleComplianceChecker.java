package gov.healthit.chpl.app.surveillance.rules;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.OversightRuleResult;

public abstract class SurveillanceRuleComplianceChecker implements RuleChecker {
	private int daysUntilOngoing;
	
	public abstract OversightRuleResult check(CertifiedProductSearchDetails cp, Surveillance surv);

	public int getDaysUntilOngoing() {
		return daysUntilOngoing;
	}

	public void setDaysUntilOngoing(int daysUntilOngoing) {
		this.daysUntilOngoing = daysUntilOngoing;
	}
}
