package gov.healthit.chpl.app.surveillance.rules;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.domain.OversightRuleResult;

@Component(value="longSuspensionComplianceChecker")
public class LongSuspensionComplianceChecker extends SurveillanceRuleComplianceChecker {
	private int numDaysAllowed = 0;
	
	public SurveillanceOversightRule getRuleChecked() {
		return SurveillanceOversightRule.LONG_SUSPENSION;
	}
	public OversightRuleResult check(CertifiedProductSearchDetails cp, Surveillance surv) {
		OversightRuleResult result = OversightRuleResult.OK;
		if(cp.getCertificationStatus().get("name").equals(CertificationStatusType.SuspendedByAcb.getName())) {
			List<CertificationStatusEvent> statusEvents = cp.getCertificationEvents();
			//find the most recent one
			CertificationStatusEvent mostRecent = null;
			for(CertificationStatusEvent statusEvent : statusEvents) {
				if(mostRecent == null || 
						(statusEvent.getEventDate().longValue() > mostRecent.getEventDate().longValue())) {
					mostRecent = statusEvent;
				}
			}
			
			if(mostRecent != null) {
				LocalDateTime statusDate = LocalDateTime.ofInstant(
							Instant.ofEpochMilli(mostRecent.getEventDate()), 
						    ZoneId.systemDefault());
				Duration timeBetween = Duration.between(statusDate, LocalDateTime.now());
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
