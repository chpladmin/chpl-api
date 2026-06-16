package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class Validator2014 extends Validator {

    private int inpatientCqmCount = 0;
    private int nonCoreAmbulatory = 0;
    private int coreAmbulatory = 0;

    protected static final List<String> REQUIRED_CRITERIA = new ArrayList<String>(Arrays.asList("170.314 (a)(3)",
            "170.314 (a)(5)", "170.314 (a)(6)", "170.314 (a)(7)", "170.314 (a)(8)", "170.314 (b)(7)", "170.314 (c)(1)",
            "170.314 (c)(2)", "170.314 (c)(3)", "170.314 (d)(1)", "170.314 (d)(2)", "170.314 (d)(3)", "170.314 (d)(4)",
            "170.314 (d)(5)", "170.314 (d)(6)", "170.314 (d)(7)", "170.314 (d)(8)", "170.314 (g)(4)"));

    protected static final List<String> CPOE_CRITERIA = new ArrayList<String>(Arrays.asList("170.314 (a)(1)",
            "170.314 (a)(18)", "170.314 (a)(19)", "170.314 (a)(20)"));

    protected static final List<String> INPATIENT_CQMS = new ArrayList<String>(Arrays.asList("CMS9", "CMS26", "CMS30",
            "CMS31", "CMS32", "CMS53", "CMS55", "CMS60", "CMS71", "CMS72", "CMS73", "CMS91", "CMS100", "CMS102",
            "CMS104", "CMS105", "CMS107", "CMS108", "CMS109", "CMS110", "CMS111", "CMS113", "CMS114", "CMS171",
            "CMS172", "CMS178", "CMS185", "CMS188", "CMS190"));

    protected static final List<String> AMBULATORY_CQMS = new ArrayList<String>(Arrays.asList(
    // Core "CMS2",
            "CMS22",
            // Core "CMS50",
            "CMS52", "CMS56", "CMS61", "CMS62", "CMS64", "CMS65", "CMS66",
            // Core "CMS68",
            // Core "CMS69",
            "CMS74",
            // Core "CMS75",
            "CMS77", "CMS82",
            // Core "CMS90",
            // Core "CMS117",
            "CMS122", "CMS123", "CMS124", "CMS125",
            // Core "CMS126",
            "CMS127", "CMS128", "CMS129", "CMS130", "CMS131", "CMS132", "CMS133", "CMS134", "CMS135",
            // Core "CMS136",
            "CMS137",
            // Core "CMS138",
            "CMS139", "CMS140", "CMS141", "CMS142", "CMS143", "CMS144", "CMS145",
            // Core "CMS146",
            "CMS147", "CMS148", "CMS149",
            // Core "CMS153",
            // Core "CMS154",
            // Core "CMS155",
            // Core "CMS156",
            "CMS157", "CMS158", "CMS159", "CMS160", "CMS161", "CMS163", "CMS164",
            // Core "CMS165",
            // Core "CMS166",
            "CMS167", "CMS169", "CMS177", "CMS179", "CMS182"));

    protected static final List<String> AMBULATORY_CORE_CQMS = new ArrayList<String>(Arrays.asList("CMS2", "CMS50",
            "CMS68", "CMS69", "CMS75", "CMS90", "CMS117", "CMS126", "CMS136", "CMS138", "CMS146", "CMS153", "CMS154",
            "CMS155", "CMS156", "CMS165", "CMS166"));

    public Validator2014() {
        this.getCounts().setCriteriaRequired(REQUIRED_CRITERIA.size());
        this.getCounts().setCriteriaRequiredMet(0);
        this.getCounts().setCriteriaCpoeRequired(1);
        this.getCounts().setCriteriaCpoeRequiredMet(0);
        this.getCounts().setCriteriaTocRequired(2);
        this.getCounts().setCriteriaTocRequiredMet(0);
        this.getCounts().setCqmsInpatientRequired(16);
        this.getCounts().setCqmsInpatientRequiredMet(0);
        this.getCounts().setCqmsAmbulatoryRequired(3);
        this.getCounts().setCqmsAmbulatoryRequiredMet(0);
        this.getCounts().setCqmsAmbulatoryCoreRequired(6);
        this.getCounts().setCqmsAmbulatoryCoreRequiredMet(0);
        this.getCounts().setDomainsRequired(3);
        this.getCounts().setDomainsRequiredMet(0);
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
        this.getCounts().setCriteriaRequired(REQUIRED_CRITERIA.size());
        boolean criteriaValid = true;
        for (String crit : REQUIRED_CRITERIA) {
            if (!criteriaMetContainsCriterion(crit)) {
                criteriaValid = false;
                getMissingAnd().add(crit);
            } else {
                this.getCounts().setCriteriaRequiredMet(this.getCounts().getCriteriaRequiredMet() + 1);
            }
        }

        boolean cpoeValid = isCPOEValid();
        boolean tocValid = isTOCValid();

        this.getCounts().setCriteriaRequired(
                this.getCounts().getCriteriaRequired() + this.getCounts().getCriteriaCpoeRequired()
                        + this.getCounts().getCriteriaTocRequired());
        this.getCounts().setCriteriaRequiredMet(
                this.getCounts().getCriteriaRequiredMet() + this.getCounts().getCriteriaCpoeRequiredMet()
                        + this.getCounts().getCriteriaTocRequiredMet());

        return (criteriaValid && cpoeValid && tocValid);
    }

    // **********************************************************************
    // isCqmsValid
    //
    // Either Inpatient or Ambulatory CQMs required.
    // **********************************************************************
    protected boolean isCqmsValid() {
        boolean valid = false;
        if (isAmbulatoryCqmsValid()) {
            valid = true;
        }
        if (isInpatientCqmsValid()) {
            valid = true;
        }
        if (!valid) {
            if (this.getCounts().getCqmsInpatientRequiredMet() < this.getCounts().getCqmsInpatientRequired()) {
                String needed = String.valueOf((this.getCounts().getCqmsInpatientRequired() - inpatientCqmCount));
                TreeMap<String, ArrayList<String>> missingInpatient = new TreeMap<String, ArrayList<String>>();
                missingInpatient.put(needed, (ArrayList<String>) INPATIENT_CQMS);
                getMissingXOr().add(missingInpatient);
            }
            if (coreAmbulatory < this.getCounts().getCqmsAmbulatoryCoreRequired()) {
                String missing = String.valueOf(this.getCounts().getCqmsAmbulatoryCoreRequired() - coreAmbulatory);
                TreeMap<String, ArrayList<String>> missingCoreAmbulatory = new TreeMap<String, ArrayList<String>>();
                missingCoreAmbulatory.put(missing, (ArrayList<String>) AMBULATORY_CORE_CQMS);
                getMissingXOr().add(missingCoreAmbulatory);
            } else if ((this.getCounts().getCqmsAmbulatoryRequiredMet() + this.getCounts().getCqmsAmbulatoryCoreRequiredMet())
                    < (this.getCounts().getCqmsAmbulatoryRequired() + this.getCounts().getCqmsAmbulatoryCoreRequired())) {
                String missing = String.valueOf((this.getCounts().getCqmsAmbulatoryCoreRequired() + this.getCounts()
                        .getCqmsAmbulatoryRequired()) - (coreAmbulatory + nonCoreAmbulatory));
                TreeMap<String, ArrayList<String>> missingAmbulatory = new TreeMap<String, ArrayList<String>>();
                ArrayList<String> combined = new ArrayList<String>();
                combined.addAll(AMBULATORY_CORE_CQMS);
                combined.addAll(AMBULATORY_CQMS);
                missingAmbulatory.put(missing, combined);
                getMissingXOr().add(missingAmbulatory);
            }
        }
        return valid;
    }

    // **********************************************************************
    // isDomainsValid
    //
    // At least 3 CQM Domains must be met.
    // **********************************************************************
    protected boolean isDomainsValid() {
        this.getCounts().setDomainsRequiredMet(
                this.getDomainsMet().size() >= this.getCounts().getDomainsRequired() ? this.getCounts().getDomainsRequired()
                        : this.getDomainsMet().size());
        return (this.getCounts().getDomainsRequiredMet() >= this.getCounts().getDomainsRequired());
    }

    // **********************************************************************
    // isInpatientCqmsValid
    //
    // At least 16 Inpatient CQMs must be met.
    // **********************************************************************
    protected boolean isInpatientCqmsValid() {
        for (String cqm : INPATIENT_CQMS) {
            if (this.getCqmsMet().contains(cqm)) {
                ++inpatientCqmCount;
            }
        }
        this.getCounts().setCqmsInpatientRequiredMet(inpatientCqmCount);
        return (this.getCounts().getCqmsInpatientRequiredMet() >= this.getCounts().getCqmsInpatientRequired());
    }

    // **********************************************************************
    // isAmbulatoryCqmsValid
    //
    // At least 9 total Ambulatory CQMs with at least 6 of those being
    // Ambulatory Core CQMs.
    // §170.102 Definitions
    // **********************************************************************
    protected boolean isAmbulatoryCqmsValid() {
        int nonCoreAmbulatory = 0;
        int coreAmbulatory = 0;

        for (String cqm : getCqmsMet()) {
            if (AMBULATORY_CORE_CQMS.contains(cqm)) {
                ++coreAmbulatory;
            }
            if (AMBULATORY_CQMS.contains(cqm)) {
                ++nonCoreAmbulatory;
            }
        }
        this.getCounts().setCqmsAmbulatoryRequiredMet(nonCoreAmbulatory);
        this.getCounts().setCqmsAmbulatoryCoreRequiredMet(coreAmbulatory);

        return (this.getCounts().getCqmsAmbulatoryCoreRequiredMet() >= this.getCounts().getCqmsAmbulatoryCoreRequired())
                && ((this.getCounts().getCqmsAmbulatoryRequiredMet() + this.getCounts().getCqmsAmbulatoryCoreRequiredMet())
                        >= (this.getCounts().getCqmsAmbulatoryRequired() + this.getCounts().getCqmsAmbulatoryCoreRequired()));
    }

    // **********************************************************************
    // isCPOEValid
    //
    // At least one of the four Computerized Provider Order Entry-related
    // criteria must be met.
    // **********************************************************************
    protected boolean isCPOEValid() {
        for (String crit : CPOE_CRITERIA) {
            if (criteriaMetContainsCriterion(crit)) {
                this.getCounts().setCriteriaCpoeRequiredMet(1);
                return true;
            }
        }
        getMissingOr().add(new ArrayList<String>(CPOE_CRITERIA));
        return false;
    }

    // **********************************************************************
    // isTOCValid
    //
    // A combination of the Transitions of Care criteria must be met.
    // **********************************************************************
    protected boolean isTOCValid() {

        // 170.314(b)(1) and 170.314(b)(2) and 170.314(b)(8) and 170.314(h)(1)
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.314 (h)(1)")) {
            this.getCounts().setCriteriaTocRequiredMet(4);
            this.getCounts().setCriteriaTocRequired(4);
            return true;
        }

        // 170.314(b)(1) and 170.314(b)(2) and 170.314(h)(1)
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (h)(1)")) {
            this.getCounts().setCriteriaTocRequiredMet(3);
            this.getCounts().setCriteriaTocRequired(3);
            return true;
        }

        // 170.314(b)(1) and 170.314(b)(2) and 170.314(b)(8)
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (b)(8)")) {
            this.getCounts().setCriteriaTocRequiredMet(3);
            this.getCounts().setCriteriaTocRequired(3);
            return true;
        }

        // 170.314(b)(8) and 170.314(h)(1)
        if (criteriaMetContainsCriterion("170.314 (b)(8)") && this.criteriaMetContainsCriterion("170.314 (h)(1)")) {
            this.getCounts().setCriteriaTocRequiredMet(2);
            this.getCounts().setCriteriaTocRequired(2);
            return true;
        }

        // 170.314(b)(1) and 170.314(b)(2)
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")) {
            this.getCounts().setCriteriaTocRequiredMet(2);
            this.getCounts().setCriteriaTocRequired(2);
            return true;
        }

        getMissingCombo().add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(8)", "170.314 (h)(1)")));
        getMissingCombo().add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (h)(1)")));
        getMissingCombo().add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(8)")));
        getMissingCombo().add(new ArrayList<String>(Arrays.asList("170.314 (b)(8)", "170.314 (h)(1)")));
        getMissingCombo().add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)")));

        return false;
    }
}
