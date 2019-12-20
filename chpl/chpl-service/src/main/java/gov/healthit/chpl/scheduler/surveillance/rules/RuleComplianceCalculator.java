package gov.healthit.chpl.scheduler.surveillance.rules;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.OversightRuleResult;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;

@Component("ruleComplianceCalculator")
public class RuleComplianceCalculator {
    @Autowired
    private LongSuspensionComplianceChecker lsc;
    @Autowired
    private CapApprovalComplianceChecker capApproval;
    @Autowired
    private CapStartedComplianceChecker capStarted;
    @Autowired
    private CapCompletedComplianceChecker capCompleted;
    @Autowired
    private CapClosedComplianceChecker capClosed;
    @Autowired
    private NonconformityOpenCapCompleteComplianceChecker ncOpenCapClosed;
    @Autowired
    private Environment env;

    public RuleComplianceCalculator() {
    }

    public List<OversightRuleResult> calculateCompliance(CertifiedProductSearchDetails cp, Surveillance surv,
            SurveillanceNonconformity nc) {
        setProps();

        List<OversightRuleResult> survRuleResults = new ArrayList<OversightRuleResult>();

        OversightRuleResult lscResult = new OversightRuleResult();
        lscResult.setRule(lsc.getRuleChecked());
        lscResult.setDateBroken(lsc.check(cp, surv, null));
        survRuleResults.add(lscResult);

        if (nc != null) {
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

            OversightRuleResult capClosedResult = new OversightRuleResult();
            capClosedResult.setRule(capClosed.getRuleChecked());
            capClosedResult.setDateBroken(capClosed.check(cp, surv, nc));
            survRuleResults.add(capClosedResult);

            OversightRuleResult ncOpenCapClosedResult = new OversightRuleResult();
            ncOpenCapClosedResult.setRule(ncOpenCapClosed.getRuleChecked());
            ncOpenCapClosedResult.setDateBroken(ncOpenCapClosed.check(cp, surv, nc));
            survRuleResults.add(ncOpenCapClosedResult);
        }
        return survRuleResults;
    }

    private void setProps() {
        lsc.setNumDaysAllowed(Integer.parseInt(env.getProperty("suspendedDaysAllowed")));
        capApproval.setNumDaysAllowed(Integer.parseInt(env.getProperty("capApprovalDaysAllowed")));
        capStarted.setNumDaysAllowed(Integer.parseInt(env.getProperty("capStartDaysAllowed")));
        ncOpenCapClosed.setNumDaysAllowed(Integer.parseInt(env.getProperty("ncOpenCapClosedDaysAllowed")));
    }

    public LongSuspensionComplianceChecker getLsc() {
        return lsc;
    }

    public void setLsc(final LongSuspensionComplianceChecker lsc) {
        this.lsc = lsc;
    }

    public CapApprovalComplianceChecker getCapApproval() {
        return capApproval;
    }

    public void setCapApproval(final CapApprovalComplianceChecker capApproval) {
        this.capApproval = capApproval;
    }

    public CapStartedComplianceChecker getCapStarted() {
        return capStarted;
    }

    public void setCapStarted(final CapStartedComplianceChecker capStarted) {
        this.capStarted = capStarted;
    }

    public CapCompletedComplianceChecker getCapCompleted() {
        return capCompleted;
    }

    public void setCapCompleted(final CapCompletedComplianceChecker capCompleted) {
        this.capCompleted = capCompleted;
    }

    public NonconformityOpenCapCompleteComplianceChecker getNcOpenCapClosed() {
        return ncOpenCapClosed;
    }

    public void setNcOpenCapClosed(final NonconformityOpenCapCompleteComplianceChecker ncOpenCapClosed) {
        this.ncOpenCapClosed = ncOpenCapClosed;
    }
}
