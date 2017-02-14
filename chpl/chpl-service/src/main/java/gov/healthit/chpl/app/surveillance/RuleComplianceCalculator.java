package gov.healthit.chpl.app.surveillance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.surveillance.rules.CapApprovalComplianceChecker;
import gov.healthit.chpl.app.surveillance.rules.CapCompletedComplianceChecker;
import gov.healthit.chpl.app.surveillance.rules.CapStartedComplianceChecker;
import gov.healthit.chpl.app.surveillance.rules.LongSuspensionComplianceChecker;
import gov.healthit.chpl.app.surveillance.rules.NonconformityRuleComplianceChecker;
import gov.healthit.chpl.app.surveillance.rules.RuleChecker;
import gov.healthit.chpl.app.surveillance.rules.SurveillanceRuleComplianceChecker;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.domain.OversightRuleResult;

@Component("ruleComplianceCalculator")
public class RuleComplianceCalculator {
	private Properties props;
	private int numDaysUntilOngoing;
	
	@Autowired private LongSuspensionComplianceChecker lsc;
	@Autowired private CapApprovalComplianceChecker capApproval;
	@Autowired private CapStartedComplianceChecker capStarted;
	@Autowired private CapCompletedComplianceChecker capCompleted;
	
	private List<RuleChecker> ruleCheckers;
	
	public RuleComplianceCalculator() {
		ruleCheckers = new ArrayList<RuleChecker>();
		ruleCheckers.add(lsc);
		ruleCheckers.add(capApproval);
		ruleCheckers.add(capStarted);
		ruleCheckers.add(capCompleted);
	}
	
	public Map<SurveillanceOversightRule, OversightRuleResult> calculateCompliance(CertifiedProductSearchDetails cp, Surveillance surv) {
		Map<SurveillanceOversightRule, OversightRuleResult> ruleCompliance
			= new HashMap<SurveillanceOversightRule, OversightRuleResult>();
		
		for(RuleChecker checker : ruleCheckers) {
			if(checker instanceof SurveillanceRuleComplianceChecker) {
				SurveillanceRuleComplianceChecker survChecker = (SurveillanceRuleComplianceChecker) checker;
				OversightRuleResult result = survChecker.check(cp, surv);
				ruleCompliance.put(checker.getRuleChecked(), result);
			}
		}
		return ruleCompliance;
	}

	public Map<SurveillanceOversightRule, OversightRuleResult> calculateCompliance(
			CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
		Map<SurveillanceOversightRule, OversightRuleResult> ruleCompliance
			= new HashMap<SurveillanceOversightRule, OversightRuleResult>();
		
		for(RuleChecker checker : ruleCheckers) {
			if(checker instanceof NonconformityRuleComplianceChecker) {
				NonconformityRuleComplianceChecker ncChecker = (NonconformityRuleComplianceChecker) checker;
				OversightRuleResult result = ncChecker.check(surv, nc);
				ruleCompliance.put(checker.getRuleChecked(), result);
			}
		}
		return ruleCompliance;
	}
	
	public List<RuleChecker> getRuleCheckers() {
		return ruleCheckers;
	}

	public void setRuleCheckers(List<RuleChecker> ruleCheckers) {
		this.ruleCheckers = ruleCheckers;
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
}
