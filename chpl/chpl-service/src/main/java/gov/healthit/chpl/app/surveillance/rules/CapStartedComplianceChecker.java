package gov.healthit.chpl.app.surveillance.rules;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.domain.OversightRuleResult;

@Component(value="capStartedComplianceChecker")
public class CapStartedComplianceChecker extends NonconformityRuleComplianceChecker {
	private int numDaysAllowed = 0;
	
	public SurveillanceOversightRule getRuleChecked() {
		return SurveillanceOversightRule.CAP_NOT_STARTED;
	}
	public OversightRuleResult check(Surveillance surv, SurveillanceNonconformity nc) {
		OversightRuleResult result = OversightRuleResult.OK;
		
		if(nc.getCapStartDate() == null) {
			LocalDateTime capApprovalDate = null;
			if(nc.getCapApprovalDate() != null) {
				capApprovalDate = LocalDateTime.ofInstant(
						Instant.ofEpochMilli(nc.getCapApprovalDate().getTime()), 
					    ZoneId.systemDefault());
				Duration timeBetween = Duration.between(capApprovalDate, LocalDateTime.now());
				long numDays = timeBetween.toDays();
				if(numDays == getNumDaysAllowed()+getDaysUntilOngoing()) {
					result = OversightRuleResult.NEW;
				} else if(numDays > getNumDaysAllowed()+getDaysUntilOngoing()) {
					result = OversightRuleResult.ONGOING;
				}
			}
		}
		
		return result;
	}
	
	public int getNumDaysAllowed() {
		return numDaysAllowed;
	}
	public void setNumDaysAllowed(int numDaysAllowed) {
		this.numDaysAllowed = numDaysAllowed;
	}
}
