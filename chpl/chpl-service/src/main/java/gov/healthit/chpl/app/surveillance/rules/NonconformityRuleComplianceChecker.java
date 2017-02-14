package gov.healthit.chpl.app.surveillance.rules;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.OversightRuleResult;

public abstract class NonconformityRuleComplianceChecker implements RuleChecker {
	private int daysUntilOngoing;
	
	public abstract OversightRuleResult check(Surveillance surv, SurveillanceNonconformity nc);

	public int getDaysUntilOngoing() {
		return daysUntilOngoing;
	}

	public void setDaysUntilOngoing(int daysUntilOngoing) {
		this.daysUntilOngoing = daysUntilOngoing;
	}
	
}
