package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Validator2015Legacy extends Validator {

    protected static final List<String> REQUIRED_CRITERIA = new ArrayList<String>(Arrays.asList("170.315 (a)(5)",
            "170.315 (a)(6)", "170.315 (a)(7)", "170.315 (a)(8)", "170.315 (a)(9)", "170.315 (a)(11)",
            "170.315 (a)(14)", "170.315 (c)(1)", "170.315 (b)(1)", "170.315 (b)(6)", "170.315 (g)(7)",
            "170.315 (g)(8)", "170.315 (g)(9)"));

    protected static final List<String> CPOE_CRITERIA_OR = new ArrayList<String>(Arrays.asList("170.315 (a)(1)",
            "170.315 (a)(2)", "170.315 (a)(3)"));

    public Validator2015Legacy() {
        this.counts.put("criteriaRequired", REQUIRED_CRITERIA.size());
        this.counts.put("criteriaRequiredMet", 0);
        this.counts.put("criteriaCpoeRequired", 1);
        this.counts.put("criteriaCpoeRequiredMet", 0);
        this.counts.put("criteriaDpRequired", 1);
        this.counts.put("criteriaDpRequiredMet", 0);
        this.counts.put("cqmsInpatientRequired", 0);
        this.counts.put("cqmsInpatientRequiredMet", 0);
        this.counts.put("cqmsAmbulatoryRequired", 0);
        this.counts.put("cqmsAmbulatoryRequiredMet", 0);
        this.counts.put("cqmsAmbulatoryCoreRequired", 0);
        this.counts.put("cqmsAmbulatoryCoreRequiredMet", 0);
        this.counts.put("domainsRequired", 0);
        this.counts.put("domainsRequiredMet", 0);
    }

    // **********************************************************************
    // onValidate
    //
    // **********************************************************************
    public boolean onValidate() {
        boolean crit = isCriteriaValid();
        boolean cqms = isCqmsValid();
        boolean domains = isDomainsValid();
        return (crit && cqms && domains);
    }

    // **********************************************************************
    // isCriteriaValid
    //
    // Must meet all required criteria.
    // **********************************************************************
    protected boolean isCriteriaValid() {
        this.counts.put("criteriaRequired", REQUIRED_CRITERIA.size());
        boolean criteriaValid = true;
        for (String crit : REQUIRED_CRITERIA) {
            if (!criteriaMetContainsCriterion(crit)) {
                missingAnd.add(crit);
                criteriaValid = false;
            } else {
                this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            }
        }

        boolean cpoeValid = isCPOEValid();
        boolean dpValid = isDPValid();

        this.counts.put(
                "criteriaRequired",
                this.counts.get("criteriaRequired") + this.counts.get("criteriaCpoeRequired")
                        + this.counts.get("criteriaDpRequired"));
        this.counts.put(
                "criteriaRequiredMet",
                this.counts.get("criteriaRequiredMet") + this.counts.get("criteriaCpoeRequiredMet")
                        + this.counts.get("criteriaDpRequiredMet"));

        return (criteriaValid && cpoeValid && dpValid);
    }

    // **********************************************************************
    // isCqmsValid
    //
    // There is no CQM check for 2015 attestation.
    // **********************************************************************
    protected boolean isCqmsValid() {
        return true;
    }

    // **********************************************************************
    // isDomainsValid
    //
    // There is no domain check for 2015 attestation.
    // **********************************************************************
    protected boolean isDomainsValid() {
        return true;
    }

    // **********************************************************************
    // isCPOEValid
    //
    // At least one of the four Computerized Provider Order Entry-related
    // criteria must be met.
    // **********************************************************************
    protected boolean isCPOEValid() {
        for (String crit : CPOE_CRITERIA_OR) {
            if (criteriaMetContainsCriterion(crit)) {
                this.counts.put("criteriaCpoeRequiredMet", 1);
                return true;
            }
        }
        missingOr.add(new ArrayList<String>(CPOE_CRITERIA_OR));
        return false;
    }

    // **********************************************************************
    // isDPValid
    //
    // Either Direct Project or Direct Project, Edge Protocol, and
    // XDR/XDM must be met.
    // **********************************************************************
    protected boolean isDPValid() {
        this.counts.put("criteriaDpRequired", 1);

        boolean met = false;

        // 170.315 (h)(1)
        if (criteriaMetContainsCriterion("170.315 (h)(1)")) {
            this.counts.put("criteriaDpRequiredMet", 1);
            met = true;
        }

        // 170.315 (h)(2)
        if (criteriaMetContainsCriterion("170.315 (h)(2)")) {
            this.counts.put("criteriaDpRequiredMet", 1);
            met = true;
        }

        if (!met) {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.315 (h)(1)", "170.315 (h)(2)")));
        }

        return (this.counts.get("criteriaDpRequiredMet") >= this.counts.get("criteriaDpRequired"));
    }
}
