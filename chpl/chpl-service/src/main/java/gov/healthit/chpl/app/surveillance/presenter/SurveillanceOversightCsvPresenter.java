package gov.healthit.chpl.app.surveillance.presenter;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.surveillance.RuleComplianceCalculator;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.OversightRuleResult;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceOversightRule;

/**
 * writes out only surveillance records that broke a certain set of rules
 * @author kekey
 *
 */
@Component("surveillanceOversightCsvPresenter")
public class SurveillanceOversightCsvPresenter extends SurveillanceReportCsvPresenter {
	private static final Logger logger = LogManager.getLogger(SurveillanceOversightCsvPresenter.class);
	private int numDaysUntilOngoing;
	@Autowired private RuleComplianceCalculator ruleCalculator;
	
	public SurveillanceOversightCsvPresenter() {
	}
	
	@Override
	protected List<String> generateHeaderValues() {
		List<String> result = super.generateHeaderValues();
		result.add(13, SurveillanceOversightRule.LONG_SUSPENSION.getTitle());
		result.add(14, SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle());
		result.add(15, SurveillanceOversightRule.CAP_NOT_STARTED.getTitle());
		result.add(16, SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle());
		return result;
	}
	
	@Override
	protected List<String> getNoNonconformityFields(CertifiedProductSearchDetails data, Surveillance surv) {
		List<String> ncFields = super.getNoNonconformityFields(data, surv);
		Map<SurveillanceOversightRule, OversightRuleResult> oversightResult = 
				ruleCalculator.calculateCompliance(data, surv);
		
		if(oversightResult != null) {
			ncFields.add(2, oversightResult.getOrDefault(SurveillanceOversightRule.LONG_SUSPENSION, OversightRuleResult.OK).toString());
		}
		//no caps on this row so these next rules are all n/a
		ncFields.add(3, "N/A");
		ncFields.add(4, "N/A");
		ncFields.add(5, "N/A");
		return ncFields;
	}
	
	@Override
	protected List<String> getNonconformityFields(CertifiedProductSearchDetails data, Surveillance surv, SurveillanceNonconformity nc) {
		List<String> ncFields = super.getNoNonconformityFields(data, surv);		
		Map<SurveillanceOversightRule, OversightRuleResult> oversightResult = 
				ruleCalculator.calculateCompliance(data, surv);
		
		if(oversightResult != null) {
			ncFields.add(2, oversightResult.getOrDefault(SurveillanceOversightRule.LONG_SUSPENSION, OversightRuleResult.OK).toString());
		}
		
		oversightResult = ruleCalculator.calculateCompliance(data, surv, nc);
		ncFields.add(3, oversightResult.getOrDefault(SurveillanceOversightRule.CAP_NOT_APPROVED, OversightRuleResult.OK).toString());
		ncFields.add(4, oversightResult.getOrDefault(SurveillanceOversightRule.CAP_NOT_STARTED, OversightRuleResult.OK).toString());
		ncFields.add(5, oversightResult.getOrDefault(SurveillanceOversightRule.CAP_NOT_COMPLETED, OversightRuleResult.OK).toString());
		
		return ncFields;
	}

	public int getNumDaysUntilOngoing() {
		return numDaysUntilOngoing;
	}

	public void setNumDaysUntilOngoing(int numDaysUntilOngoing) {
		this.numDaysUntilOngoing = numDaysUntilOngoing;
		ruleCalculator.setNumDaysUntilOngoing(this.numDaysUntilOngoing);
	}
	
	@Override
	public void setProps(Properties props) {
		super.setProps(props);
		ruleCalculator.setProps(props);
	}
}
