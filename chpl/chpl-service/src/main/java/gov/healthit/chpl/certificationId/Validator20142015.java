package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.Arrays;

public class Validator20142015 extends Validator {

    public Validator20142015() {
        this.counts.put("criteriaRequired", 0); // This number is calculated
                                                // during the checks
        this.counts.put("criteriaRequiredMet", 0);
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
        this.counts.put("criteriaRequired", 7 + 3 + 8);
        // 7 categories 1 pt per category + 3 pts for cqm category + 8 pts for
        // ps category

        boolean cpoe = false;
        boolean recordDemo = false;
        boolean problemList = false;
        boolean medList = false;
        boolean medAllergyList = false;
        boolean clinicalDecision = false;

        // (1)(i) 45 CFR 170.314(a)(1), (18), (19) or (20); or (ii) 45 CFR
        // 170.315(a)(1), (2) or (3).
        if ((criteriaMetContainsCriterion("170.314 (a)(1)") || criteriaMetContainsCriterion("170.314 (a)(18)")
                || criteriaMetContainsCriterion("170.314 (a)(19)") || criteriaMetContainsCriterion("170.314 (a)(20)"))
                || (criteriaMetContainsCriterion("170.315 (a)(1)") || criteriaMetContainsCriterion("170.315 (a)(2)")
                        || criteriaMetContainsCriterion("170.315 (a)(3)"))) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            cpoe = true;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (a)(1)", "170.314 (a)(18)", "170.314 (a)(19)",
                    "170.314 (a)(20)", "170.315 (a)(1)", "170.315 (a)(2)", "170.315 (a)(3)")));
        }

        // (2)(i) Record demographics at 45 CFR 170.314(a)(3); or (ii) 45 CFR
        // 170.315(a)(5).
        if (criteriaMetContainsCriterion("170.314 (a)(3)") || criteriaMetContainsCriterion("170.315 (a)(5)")) {
            recordDemo = true;
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (a)(3)", "170.315 (a)(5)")));
        }

        // (3)(i) Problem list at 45 CFR 170.314(a)(5); or (ii) 45 CFR
        // 170.315(a)(6).
        if (criteriaMetContainsCriterion("170.314 (a)(5)") || criteriaMetContainsCriterion("170.315 (a)(6)")) {
            problemList = true;
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (a)(5)", "170.315 (a)(6)")));
        }

        // (4)(i) Medication list at 45 CFR 170.314(a)(6); or (ii) 45 CFR
        // 170.315(a)(7).
        if (criteriaMetContainsCriterion("170.314 (a)(6)") || criteriaMetContainsCriterion("170.315 (a)(7)")) {
            medList = true;
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (a)(6)", "170.315 (a)(7)")));
        }

        // (5)(i) Medication allergy list 45 CFR 170.314(a)(7); or (ii) 45 CFR
        // 170.315(a)(8).
        if (criteriaMetContainsCriterion("170.314 (a)(7)") || criteriaMetContainsCriterion("170.315 (a)(8)")) {
            medAllergyList = true;
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (a)(7)", "170.315 (a)(8)")));
        }

        // (6)(i) Clinical decision support at 45 CFR 170.314(a)(8); or (ii) 45
        // CFR 170.315(a)(9).
        if (criteriaMetContainsCriterion("170.314 (a)(8)") || criteriaMetContainsCriterion("170.315 (a)(9)")) {
            clinicalDecision = true;
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (a)(8)", "170.315 (a)(9)")));
        }

        boolean critToc = this.isCriteriaTOCValid();
        boolean critCqm = this.isCriteriaCqmValid();
        boolean critPs = this.isCriteriaPSValid();

        return (cpoe && recordDemo && problemList && medList && medAllergyList && clinicalDecision && critToc
                && critCqm && critPs);
    }

    // **********************************************************************
    // isCriteriaTOCValid
    //
    // Must match at least one of the 21 sets.
    //
    // Checks should be performed in the order of sets with the most
    // required criteria to the sets with the least required criteria.
    // **********************************************************************
    protected boolean isCriteriaTOCValid() {
        boolean flag = false;

        // (xvi) 45 CFR 170.314(b)(1), (b)(2), (b)(8), (h)(1), 170.315(b)(1),
        // and 170.315(h)(1).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.314 (h)(1)")
                && criteriaMetContainsCriterion("170.315 (b)(1)") && criteriaMetContainsCriterion("170.315 (h)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (xvii) 45 CFR 170.314(b)(1), (b)(2), (b)(8), (h)(1), 170.315(b)(1),
        // and 170.315(h)(2).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.314 (h)(1)")
                && criteriaMetContainsCriterion("170.315 (b)(1)") && criteriaMetContainsCriterion("170.315 (h)(2)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (ix) 45 CFR 170.314(b)(1), (b)(2), (b)(8), (h)(1), and 170.315(h)(2).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.314 (h)(1)")
                && criteriaMetContainsCriterion("170.315 (h)(2)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (xiv) 45 CFR 170.314(b)(1), (b)(2),(b)(8), (h)(1), and 170.315(b)(1).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.314 (h)(1)")
                && criteriaMetContainsCriterion("170.315 (b)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (iv) 45 CFR 170.314(b)(1), (b)(2), (b)(8), and (h)(1).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.314 (h)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (vii) 45 CFR 170.314(b)(1), (b)(2), (h)(1), and 170.315(h)(2).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (h)(1)") && criteriaMetContainsCriterion("170.315 (h)(2)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (viii) 45 CFR 170.314(b)(1), (b)(2), (b)(8), and 170.315(h)(2).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.315 (h)(2)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (xii) 45 CFR 170.314(b)(1), (b)(2), (h)(1), and 170.315(b)(1).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (h)(1)") && criteriaMetContainsCriterion("170.315 (b)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (xiii) 45 CFR 170.314(b)(1), (b)(2), (b)(8), and 170.315(b)(1).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.315 (b)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (ii) 45 CFR 170.314(b)(1), (b)(2), and (h)(1).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (h)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (iii) 45 CFR 170.314(b)(1), (b)(2), and (b)(8).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.314 (b)(8)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (vi) 45 CFR 170.314(b)(1), (b)(2), and 170.315(h)(2).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.315 (h)(2)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (x) 45 CFR 170.314(b)(8), (h)(1), and 170.315(h)(2).
        if (criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.314 (h)(1)")
                && criteriaMetContainsCriterion("170.315 (h)(2)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (xi) 45 CFR 170.314(b)(1), (b)(2), and 170.315(b)(1).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")
                && criteriaMetContainsCriterion("170.315 (b)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (xv) 45 CFR 170.314(b)(8), (h)(1), and 170.315(b)(1).
        if (criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.314 (h)(1)")
                && criteriaMetContainsCriterion("170.315 (b)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (xxi) 45 CFR 170.315(b)(1), (h)(1), and (h)(2)
        if (criteriaMetContainsCriterion("170.315 (b)(1)") && criteriaMetContainsCriterion("170.315 (h)(1)")
                && criteriaMetContainsCriterion("170.315 (h)(2)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (v) 45 CFR 170.314(b)(8) and (h)(1).
        if (criteriaMetContainsCriterion("170.314 (b)(8)") && criteriaMetContainsCriterion("170.314 (h)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (i) 45 CFR 170.314(b)(1) and (2).
        if (criteriaMetContainsCriterion("170.314 (b)(1)") && criteriaMetContainsCriterion("170.314 (b)(2)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (xviii) 45 CFR 170.314(h)(1) and 170.315(b)(1).
        if (criteriaMetContainsCriterion("170.314 (h)(1)") && criteriaMetContainsCriterion("170.315 (b)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (xix) 45 CFR 170.315(b)(1) and (h)(1).
        if (criteriaMetContainsCriterion("170.315 (b)(1)") && criteriaMetContainsCriterion("170.315 (h)(1)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        // (xx) 45 CFR 170.315(b)(1) and (h)(2).
        if (criteriaMetContainsCriterion("170.315 (b)(1)") && criteriaMetContainsCriterion("170.315 (h)(2)")) {
            this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            flag = true;
            return true;
        }

        if (!flag) {
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(8)", "170.314 (h)(1)",
                    "170.315 (b)(1)", "170.315 (h)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(8)", "170.314 (h)(1)",
                    "170.315 (b)(1)", "170.315 (h)(2)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(8)", "170.314 (h)(1)",
                    "170.315 (h)(2)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(8)", "170.314 (h)(1)",
                    "170.315 (b)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(8)", "170.314 (h)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (h)(1)", "170.315 (h)(2)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(8)", "170.315 (h)(2)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (h)(1)", "170.315 (b)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(8)", "170.315 (b)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.315 (h)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.314 (b)(8)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.315 (h)(2)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(8)", "170.314 (h)(1)", "170.315 (h)(2)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)", "170.315 (b)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(8)", "170.314 (h)(1)", "170.315 (b)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.315 (b)(1)", "170.315 (h)(1)", "170.315 (h)(2)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(8)", "170.314 (h)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (b)(1)", "170.314 (b)(2)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.314 (h)(1)", "170.315 (b)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.315 (b)(1)", "170.315 (h)(1)")));
            missingCombo.add(new ArrayList<String>(Arrays.asList("170.315 (b)(1)", "170.315 (h)(2)")));
        }

        return false;
    }

    // **********************************************************************
    // isCriteriaCqmValid
    //
    // Must match all 3 (c) criteria.
    // **********************************************************************
    protected boolean isCriteriaCqmValid() {
        int cqmCritCount = 0;
        int cqmCritRequired = 3;

        // (1) 45 CFR 170.314(c)(1) or 170.315(c)(1);
        if (criteriaMetContainsCriterion("170.314 (c)(1)") || criteriaMetContainsCriterion("170.315 (c)(1)")) {
            ++cqmCritCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (c)(1)", "170.315 (c)(1)")));
        }

        // (2) 45 CFR 170.314(c)(2) or 170.315(c)(2)
        if (criteriaMetContainsCriterion("170.314 (c)(2)") || criteriaMetContainsCriterion("170.315 (c)(2)")) {
            ++cqmCritCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (c)(2)", "170.315 (c)(2)")));
        }

        // (3) 45 CFR 170.314(c)(3) or 170.315(c)(3)
        if (criteriaMetContainsCriterion("170.314 (c)(3)") || criteriaMetContainsCriterion("170.315 (c)(3)")) {
            ++cqmCritCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (c)(3)", "170.315 (c)(3)")));
        }

        this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + cqmCritCount);

        return (cqmCritCount == cqmCritRequired);
    }

    // **********************************************************************
    // isCriteriaPSValid
    //
    // Must match all 8 (d) criteria.
    // **********************************************************************
    protected boolean isCriteriaPSValid() {

        int psCritMetCount = 0;
        int psCritMetRequired = 8;

        // (1) 45 CFR 170.314(d)(1) or 170.315(d)(1);
        if (criteriaMetContainsCriterion("170.314 (d)(1)") || criteriaMetContainsCriterion("170.315 (d)(1)")) {
            ++psCritMetCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (d)(1)", "170.315 (d)(1)")));
        }

        // (2) 45 CFR 170.314(d)(2) or 170.315(d)(2);
        if (criteriaMetContainsCriterion("170.314 (d)(2)") || criteriaMetContainsCriterion("170.315 (d)(2)")) {
            ++psCritMetCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (d)(2)", "170.315 (d)(2)")));
        }

        // (3) 45 CFR 170.314(d)(3) or 170.315(d)(3);
        if (criteriaMetContainsCriterion("170.314 (d)(3)") || criteriaMetContainsCriterion("170.315 (d)(3)")) {
            ++psCritMetCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (d)(3)", "170.315 (d)(3)")));
        }

        // (4) 45 CFR 170.314(d)(4) or 170.315(d)(4);
        if (criteriaMetContainsCriterion("170.314 (d)(4)") || criteriaMetContainsCriterion("170.315 (d)(4)")) {
            ++psCritMetCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (d)(4)", "170.315 (d)(4)")));
        }

        // (5) 45 CFR 170.314(d)(5) or 170.315(d)(5);
        if (criteriaMetContainsCriterion("170.314 (d)(5)") || criteriaMetContainsCriterion("170.315 (d)(5)")) {
            ++psCritMetCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (d)(5)", "170.315 (d)(5)")));
        }

        // (6) 45 CFR 170.314(d)(6) or 170.315(d)(6);
        if (criteriaMetContainsCriterion("170.314 (d)(6)") || criteriaMetContainsCriterion("170.315 (d)(6)")) {
            ++psCritMetCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (d)(6)", "170.315 (d)(6)")));
        }

        // (7) 45 CFR 170.314(d)(7) or 170.315(d)(7);
        if (criteriaMetContainsCriterion("170.314 (d)(7)") || criteriaMetContainsCriterion("170.315 (d)(7)")) {
            ++psCritMetCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (d)(7)", "170.315 (d)(7)")));
        }

        // (8) 45 CFR 170.314(d)(8) or 170.315(d)(8);
        if (criteriaMetContainsCriterion("170.314 (d)(8)") || criteriaMetContainsCriterion("170.315 (d)(8)")) {
            ++psCritMetCount;
        } else {
            missingOr.add(new ArrayList<String>(Arrays.asList("170.314 (d)(8)", "170.315 (d)(8)")));
        }

        this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + psCritMetCount);

        return (psCritMetCount == psCritMetRequired);
    }

    // **********************************************************************
    // isCqmsValid
    //
    // There is no CQM check for 2014/2015 hybrid attestation.
    // **********************************************************************
    protected boolean isCqmsValid() {
        return true;
    }

    // **********************************************************************
    // isDomainsValid
    //
    // There is no CQM check for 2014/2015 hybrid attestation.
    // **********************************************************************
    protected boolean isDomainsValid() {
        return true;
    }
}
