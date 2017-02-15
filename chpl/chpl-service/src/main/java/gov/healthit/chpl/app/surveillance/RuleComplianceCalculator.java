package gov.healthit.chpl.app.surveillance;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.surveillance.rules.CapApprovalComplianceChecker;
import gov.healthit.chpl.app.surveillance.rules.CapCompletedComplianceChecker;
import gov.healthit.chpl.app.surveillance.rules.CapStartedComplianceChecker;
import gov.healthit.chpl.app.surveillance.rules.LongSuspensionComplianceChecker;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.OversightRuleResult;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceOversightRule;

@Component("ruleComplianceCalculator")
public class RuleComplianceCalculator {
	private Properties props;
	private int numDaysUntilOngoing;
	
	@Autowired private LongSuspensionComplianceChecker lsc;
	@Autowired private CapApprovalComplianceChecker capApproval;
	@Autowired private CapStartedComplianceChecker capStarted;
	@Autowired private CapCompletedComplianceChecker capCompleted;
		
	public RuleComplianceCalculator() {
	}
	
	public Map<SurveillanceOversightRule, OversightRuleResult> calculateCompliance(CertifiedProductSearchDetails cp, Surveillance surv) {
		Map<SurveillanceOversightRule, OversightRuleResult> ruleCompliance
			= new HashMap<SurveillanceOversightRule, OversightRuleResult>();
	
		OversightRuleResult result = lsc.check(cp, surv);
		ruleCompliance.put(lsc.getRuleChecked(), result);
		return ruleCompliance;
	}

	public Map<SurveillanceOversightRule, OversightRuleResult> calculateCompliance(
			CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
		Map<SurveillanceOversightRule, OversightRuleResult> ruleCompliance
			= new HashMap<SurveillanceOversightRule, OversightRuleResult>();
		
		OversightRuleResult result = capApproval.check(surv, nc);
		ruleCompliance.put(capApproval.getRuleChecked(), result);
		result = capStarted.check(surv, nc);
		ruleCompliance.put(capStarted.getRuleChecked(), result);
		result = capCompleted.check(surv, nc);
		ruleCompliance.put(capCompleted.getRuleChecked(), result);
		return ruleCompliance;
	}

	public Properties getProps() {
		return props;
	}

	public void setProps(Properties props) {
		this.props = props;
		lsc.setNumDaysAllowed(new Integer(props.getProperty("suspendedDaysAllowed")).intValue());
		capApproval.setNumDaysAllowed(new Integer(props.getProperty("capApprovalDaysAllowed")).intValue());
		capStarted.setNumDaysAllowed(new Integer(props.getProperty("capStartDaysAllowed")).intValue());
	}

	public int getNumDaysUntilOngoing() {
		return numDaysUntilOngoing;
	}

	public void setNumDaysUntilOngoing(int numDaysUntilOngoing) {
		this.numDaysUntilOngoing = numDaysUntilOngoing;
		
		lsc.setDaysUntilOngoing(numDaysUntilOngoing);
		capApproval.setDaysUntilOngoing(numDaysUntilOngoing);
		capStarted.setDaysUntilOngoing(numDaysUntilOngoing);
		capCompleted.setDaysUntilOngoing(numDaysUntilOngoing);
	}

	public LongSuspensionComplianceChecker getLsc() {
		return lsc;
	}

	public void setLsc(LongSuspensionComplianceChecker lsc) {
		this.lsc = lsc;
	}

	public CapApprovalComplianceChecker getCapApproval() {
		return capApproval;
	}

	public void setCapApproval(CapApprovalComplianceChecker capApproval) {
		this.capApproval = capApproval;
	}

	public CapStartedComplianceChecker getCapStarted() {
		return capStarted;
	}

	public void setCapStarted(CapStartedComplianceChecker capStarted) {
		this.capStarted = capStarted;
	}

	public CapCompletedComplianceChecker getCapCompleted() {
		return capCompleted;
	}

	public void setCapCompleted(CapCompletedComplianceChecker capCompleted) {
		this.capCompleted = capCompleted;
	}
}
