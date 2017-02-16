package gov.healthit.chpl.app.surveillance.presenter;

import java.util.Iterator;
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
	private boolean includeOngoing = false;
	
	@Autowired private RuleComplianceCalculator ruleCalculator;
	
	public SurveillanceOversightCsvPresenter() {
	}
	
	@Override
	protected List<String> generateHeaderValues() {
		List<String> result = super.generateHeaderValues();
		result.add(SurveillanceOversightRule.LONG_SUSPENSION.getColumnOffset(), SurveillanceOversightRule.LONG_SUSPENSION.getTitle());
		result.add(SurveillanceOversightRule.CAP_NOT_APPROVED.getColumnOffset(), SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle());
		result.add(SurveillanceOversightRule.CAP_NOT_STARTED.getColumnOffset(), SurveillanceOversightRule.CAP_NOT_STARTED.getTitle());
		result.add(SurveillanceOversightRule.CAP_NOT_COMPLETED.getColumnOffset(), SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle());
		return result;
	}
	
	@Override
	protected List<List<String>> generateMultiRowValue(CertifiedProductSearchDetails data, Surveillance surv) {
		List<List<String>> allSurveillanceRows = super.generateMultiRowValue(data, surv);
		
		//we only want to include surveillance rows that broke one or more rules, possibly only new rules
		Iterator<List<String>> rowValueIter = allSurveillanceRows.iterator();
		boolean includeRow = false;
		while(rowValueIter.hasNext()) {
			List<String> rowValues = rowValueIter.next();
			String resultStr = rowValues.get(SurveillanceOversightRule.LONG_SUSPENSION.getColumnOffset());
			OversightRuleResult result = OversightRuleResult.valueOf(resultStr);
			if(includeOngoing && result == OversightRuleResult.NEW) {
				includeRow = true;
			} else if(result != OversightRuleResult.OK) {
				includeRow = true;
			}
			if(!includeRow) {
				rowValueIter.remove();
			}
		}
		
		return allSurveillanceRows;
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

	public boolean isIncludeOngoing() {
		return includeOngoing;
	}

	public void setIncludeOngoing(boolean includeOngoing) {
		this.includeOngoing = includeOngoing;
	}
}
