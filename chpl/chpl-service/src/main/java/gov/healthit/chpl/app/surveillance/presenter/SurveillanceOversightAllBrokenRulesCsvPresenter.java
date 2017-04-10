package gov.healthit.chpl.app.surveillance.presenter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.app.surveillance.rules.RuleComplianceCalculator;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.OversightRuleResult;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceOversightRule;
import gov.healthit.chpl.domain.SurveillanceRequirement;

/**
 * writes out only surveillance records that broke a certain set of rules
 * @author kekey
 *
 */
@Component("surveillanceOversightAllBrokenRulesCsvPresenter")
public class SurveillanceOversightAllBrokenRulesCsvPresenter extends SurveillanceReportCsvPresenter {
	private static final Logger logger = LogManager.getLogger(SurveillanceOversightAllBrokenRulesCsvPresenter.class);
	
	private Map<SurveillanceOversightRule, Integer> allBrokenRulesCounts;
	
	protected static final int CHPL_PRODUCT_OFFSET = 3;
	protected static final int LONG_SUSPENSION_COL_OFFSET = 12;
	protected static final int CAP_APPROVE_COL_OFFSET = 13;
	protected static final int CAP_START_COL_OFFSET = 14;
	protected static final int CAP_COMPLETE_COL_OFFSET = 15;
	protected static final int NONCONFORMITY_STATUS_COL_OFFSET = 17;
	
	@Autowired private RuleComplianceCalculator ruleCalculator;
	
	public SurveillanceOversightAllBrokenRulesCsvPresenter() {
		allBrokenRulesCounts = new HashMap<SurveillanceOversightRule, Integer>();
		allBrokenRulesCounts.put(SurveillanceOversightRule.LONG_SUSPENSION, 0);
		allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_APPROVED, 0);
		allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_STARTED, 0);
		allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_COMPLETED, 0);
	}
	
	@Override
	protected List<String> generateHeaderValues() {
		List<String> result = super.generateHeaderValues();
		result.add(LONG_SUSPENSION_COL_OFFSET, SurveillanceOversightRule.LONG_SUSPENSION.getTitle());
		result.add(CAP_APPROVE_COL_OFFSET, SurveillanceOversightRule.CAP_NOT_APPROVED.getTitle());
		result.add(CAP_START_COL_OFFSET, SurveillanceOversightRule.CAP_NOT_STARTED.getTitle());
		result.add(CAP_COMPLETE_COL_OFFSET, SurveillanceOversightRule.CAP_NOT_COMPLETED.getTitle());
		result.add(NONCONFORMITY_STATUS_COL_OFFSET, "Nonconformity Status");
		return result;
	}

	@Override
	protected List<List<String>> generateMultiRowValue(CertifiedProductSearchDetails data, Surveillance surv) {
		List<List<String>> results = super.generateMultiRowValue(data, surv);
		
		//we only want to include surveillance rows that broke one or more rules
		String currChplProductNumber = "";
		Iterator<List<String>> rowValueIter = results.iterator();
		while(rowValueIter.hasNext()) {
			boolean includeRow = false;
			
			List<String> rowValues = rowValueIter.next();
			String rowChplProductNumber = rowValues.get(CHPL_PRODUCT_OFFSET);
			String longSuspensionResultStr = rowValues.get(LONG_SUSPENSION_COL_OFFSET);
			String capApprovalResultStr = rowValues.get(CAP_APPROVE_COL_OFFSET);
			String capStartResultStr = rowValues.get(CAP_START_COL_OFFSET);
			String capCompletedResultStr = rowValues.get(CAP_COMPLETE_COL_OFFSET);
			
			if(!StringUtils.isEmpty(longSuspensionResultStr)) {
				if(!rowChplProductNumber.equals(currChplProductNumber)) {
					currChplProductNumber = rowChplProductNumber;
					allBrokenRulesCounts.put(SurveillanceOversightRule.LONG_SUSPENSION, allBrokenRulesCounts.get(SurveillanceOversightRule.LONG_SUSPENSION)+1);
				}
				includeRow = true;
			}
			if(!StringUtils.isEmpty(capApprovalResultStr)) {
				allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_APPROVED, allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_APPROVED)+1);
				includeRow = true;
			}
			if(!StringUtils.isEmpty(capStartResultStr)) {
				allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_STARTED, allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_STARTED)+1);
				includeRow = true;
			}
			if(!StringUtils.isEmpty(capCompletedResultStr))	{
				allBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_COMPLETED, allBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_COMPLETED)+1);
				includeRow = true;
			}
		
			if(!includeRow) {
				rowValueIter.remove();
			}
		}
		
		return results;
	}
	
	protected List<SurveillanceNonconformity> getNonconformities(SurveillanceRequirement req) {
		List<SurveillanceNonconformity> openNonconformities = new ArrayList<SurveillanceNonconformity>();
		for(SurveillanceNonconformity nonconformity : req.getNonconformities()) {
			if(nonconformity.getStatus().getName().equalsIgnoreCase(SurveillanceNonconformityStatus.OPEN)) {
				openNonconformities.add(nonconformity);
			}
		}
		return openNonconformities;
	}
	
	@Override
	protected List<String> getNoNonconformityFields(CertifiedProductSearchDetails data, Surveillance surv) {
		List<String> ncFields = super.getNoNonconformityFields(data, surv);
		List<OversightRuleResult> oversightResult = ruleCalculator.calculateCompliance(data, surv, null);
		
		if(oversightResult != null && oversightResult.size() > 0 &&
			oversightResult.get(0) != null && oversightResult.get(0).getDateBroken() != null) {
			LocalDateTime dateBroken = LocalDateTime.ofInstant(
					Instant.ofEpochMilli(oversightResult.get(0).getDateBroken().getTime()), 
				    ZoneId.systemDefault());
			ncFields.add(0, dateFormatter.format(dateBroken));
		} else {
			ncFields.add(0, "");
		}
		
		//no CAPs on this row so these next rules are all n/a
		ncFields.add(1, "");
		ncFields.add(2, "");
		ncFields.add(3, "");
		//no nonconformity was found so there is no status
		ncFields.add(5, "");
		return ncFields;
	}
	
	@Override
	protected List<String> getNonconformityFields(CertifiedProductSearchDetails data, Surveillance surv, SurveillanceNonconformity nc) {
		List<String> ncFields = super.getNonconformityFields(data, surv, nc);		
		List<OversightRuleResult> oversightResult = ruleCalculator.calculateCompliance(data, surv, nc);
		//oversightResult.addAll(ruleCalculator.calculateCompliance(data, surv, nc));
		
		for(OversightRuleResult currResult : oversightResult) {
			String dateBrokenStr = "";
			if(currResult.getDateBroken() != null) {
				LocalDateTime dateBroken = LocalDateTime.ofInstant(
						Instant.ofEpochMilli(currResult.getDateBroken().getTime()), 
					    ZoneId.systemDefault());
				dateBrokenStr = dateFormatter.format(dateBroken);
			}
			if(currResult.getRule() == SurveillanceOversightRule.LONG_SUSPENSION) {
				ncFields.add(0, dateBrokenStr);
			} else if(currResult.getRule() == SurveillanceOversightRule.CAP_NOT_APPROVED) {
				ncFields.add(1, dateBrokenStr);
			} else if(currResult.getRule() == SurveillanceOversightRule.CAP_NOT_STARTED) {
				ncFields.add(2, dateBrokenStr);
			} else if(currResult.getRule() == SurveillanceOversightRule.CAP_NOT_COMPLETED) {
				ncFields.add(3, dateBrokenStr);
			}
		}
		
		ncFields.add(5, nc.getStatus().getName());
		return ncFields;
	}
	
	@Override
	public void setProps(Properties props) {
		super.setProps(props);
		ruleCalculator.setProps(props);
	}

	public Map<SurveillanceOversightRule, Integer> getAllBrokenRulesCounts() {
		return allBrokenRulesCounts;
	}

	public void setAllBrokenRulesCounts(Map<SurveillanceOversightRule, Integer> allBrokenRulesCounts) {
		this.allBrokenRulesCounts = allBrokenRulesCounts;
	}
}
