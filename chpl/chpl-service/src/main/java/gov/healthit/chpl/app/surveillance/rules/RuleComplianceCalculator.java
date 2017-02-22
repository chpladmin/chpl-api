package gov.healthit.chpl.app.surveillance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.OversightRuleResult;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;

@Component("ruleComplianceCalculator")
public class RuleComplianceCalculator {
	private Properties props;
	
	@Autowired private LongSuspensionComplianceChecker lsc;
	@Autowired private CapApprovalComplianceChecker capApproval;
	@Autowired private CapStartedComplianceChecker capStarted;
	@Autowired private CapCompletedComplianceChecker capCompleted;
		
	public RuleComplianceCalculator() {
	}

	public List<OversightRuleResult> calculateCompliance(
			CertifiedProductSearchDetails cp, Surveillance surv, SurveillanceNonconformity nc) {
		List<OversightRuleResult> survRuleResults = new ArrayList<OversightRuleResult>();

		OversightRuleResult lscResult = new OversightRuleResult();
		lscResult.setRule(lsc.getRuleChecked());
		lscResult.setDateBroken(lsc.check(cp, surv, null));
		survRuleResults.add(lscResult);
		
		if(nc != null) {
			OversightRuleResult capApprovalResult = new OversightRuleResult();
			capApprovalResult.setRule(capApproval.getRuleChecked());
			capApprovalResult.setDateBroken(capApproval.check(cp, surv, nc));
			survRuleResults.add(capApprovalResult);
			
			OversightRuleResult capStartedResult = new OversightRuleResult();
			capStartedResult.setRule(capStarted.getRuleChecked());
			capStartedResult.setDateBroken(capStarted.check(cp, surv, nc));
			survRuleResults.add(capStartedResult);
			
			OversightRuleResult capCompletedResult = new OversightRuleResult();
			capCompletedResult.setRule(capCompleted.getRuleChecked());
			capCompletedResult.setDateBroken(capCompleted.check(cp, surv, nc));
			survRuleResults.add(capCompletedResult);
		}
		return survRuleResults;
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
