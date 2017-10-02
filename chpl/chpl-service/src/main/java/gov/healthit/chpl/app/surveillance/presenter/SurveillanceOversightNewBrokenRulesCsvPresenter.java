package gov.healthit.chpl.app.surveillance.presenter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceOversightRule;

/**
 * writes out only surveillance records that broke a certain set of rules
 * 
 * @author kekey
 *
 */
@Component("surveillanceOversightNewBrokenRulesCsvPresenter")
public class SurveillanceOversightNewBrokenRulesCsvPresenter extends SurveillanceOversightAllBrokenRulesCsvPresenter {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceOversightNewBrokenRulesCsvPresenter.class);

    private Map<SurveillanceOversightRule, Integer> newBrokenRulesCounts;

    public SurveillanceOversightNewBrokenRulesCsvPresenter() {
        newBrokenRulesCounts = new HashMap<SurveillanceOversightRule, Integer>();
        newBrokenRulesCounts.put(SurveillanceOversightRule.LONG_SUSPENSION, 0);
        newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_APPROVED, 0);
        newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_STARTED, 0);
        newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_COMPLETED, 0);
        newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_CLOSED, 0);
        newBrokenRulesCounts.put(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE, 0);
    }

    @Override
    protected List<List<String>> generateMultiRowValue(CertifiedProductSearchDetails data, Surveillance surv) {
        List<List<String>> results = super.generateMultiRowValue(data, surv);

        // we only want to include surveillance rows that
        // broke one or more rules in the last day
        Iterator<List<String>> rowValueIter = results.iterator();
        Date today = new Date();
        LocalDateTime brokenToday = LocalDateTime.ofInstant(Instant.ofEpochMilli(today.getTime()),
                ZoneId.systemDefault());
        String formattedToday = dateFormatter.format(brokenToday);

        String currChplProductNumber = "";
        while (rowValueIter.hasNext()) {
            boolean includeRow = false;

            List<String> rowValues = rowValueIter.next();
            String rowChplProductNumber = rowValues.get(CHPL_PRODUCT_OFFSET);
            String longSuspensionResultStr = rowValues.get(LONG_SUSPENSION_COL_OFFSET);
            String capApprovalResultStr = rowValues.get(CAP_APPROVE_COL_OFFSET);
            String capStartResultStr = rowValues.get(CAP_START_COL_OFFSET);
            String capCompletedResultStr = rowValues.get(CAP_COMPLETE_COL_OFFSET);
            String capClosedResultStr = rowValues.get(CAP_CLOSED_COL_OFFSET);
            String nonconformityOpenCapCompleteResultStr = rowValues.get(NONCONFORMITY_OPEN_CAP_COMPLETE_COL_OFFSET);

            if (formattedToday.equals(longSuspensionResultStr)) {
                if (!rowChplProductNumber.equals(currChplProductNumber)) {
                    currChplProductNumber = rowChplProductNumber;
                    newBrokenRulesCounts.put(SurveillanceOversightRule.LONG_SUSPENSION,
                            newBrokenRulesCounts.get(SurveillanceOversightRule.LONG_SUSPENSION) + 1);
                }
                includeRow = true;
            }
            if (formattedToday.equals(capApprovalResultStr)) {
                newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_APPROVED,
                        newBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_APPROVED) + 1);
                includeRow = true;
            }
            if (formattedToday.equals(capStartResultStr)) {
                newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_STARTED,
                        newBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_STARTED) + 1);
                includeRow = true;
            }
            if (formattedToday.equals(capCompletedResultStr)) {
                newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_COMPLETED,
                        newBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_COMPLETED) + 1);
                includeRow = true;
            }
            if (formattedToday.equals(capClosedResultStr)) {
                newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_CLOSED,
                        newBrokenRulesCounts.get(SurveillanceOversightRule.CAP_NOT_CLOSED) + 1);
                includeRow = true;
            }
            if (formattedToday.equals(nonconformityOpenCapCompleteResultStr)) {
                newBrokenRulesCounts.put(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE,
                        newBrokenRulesCounts.get(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE) + 1);
                includeRow = true;
            }

            if (!includeRow) {
                rowValueIter.remove();
            }
        }

        return results;
    }

    public Map<SurveillanceOversightRule, Integer> getNewBrokenRulesCounts() {
        return newBrokenRulesCounts;
    }

    public void setNewBrokenRulesCounts(Map<SurveillanceOversightRule, Integer> newBrokenRulesCounts) {
        this.newBrokenRulesCounts = newBrokenRulesCounts;
    }

    public void clear() {
        newBrokenRulesCounts.clear();
        newBrokenRulesCounts.put(SurveillanceOversightRule.LONG_SUSPENSION, 0);
        newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_APPROVED, 0);
        newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_STARTED, 0);
        newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_COMPLETED, 0);
        newBrokenRulesCounts.put(SurveillanceOversightRule.CAP_NOT_CLOSED, 0);
        newBrokenRulesCounts.put(SurveillanceOversightRule.NONCONFORMITY_OPEN_CAP_COMPLETE, 0);
    }
}
