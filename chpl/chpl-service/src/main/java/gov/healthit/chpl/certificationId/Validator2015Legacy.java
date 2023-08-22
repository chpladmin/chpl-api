package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.Util;

/**
 * Validator for CMS EHR ID generation for 2015 Edition, post Cures rule.
 * @author alarned
 *
 */
public class Validator2015Legacy extends Validator {

    protected static final List<String> REQUIRED_CRITERIA = new ArrayList<String>(Arrays.asList("170.315 (a)(5)",
            "170.315 (a)(9)", "170.315 (a)(14)", "170.315 (c)(1)", "170.315 (g)(7)"));

    protected static final List<String> CURES_REQUIRED_CRITERIA = new ArrayList<String>(Arrays.asList("170.315 (b)(1)",
            "170.315 (g)(9)"));

    protected List<CertificationCriterion> aaCriteriaOr;

    protected static final List<String> CPOE_CRITERIA_OR = new ArrayList<String>(Arrays.asList("170.315 (a)(1)",
            "170.315 (a)(2)", "170.315 (a)(3)"));

    protected static final List<String> DP_CRITERIA_OR = new ArrayList<String>(Arrays.asList("170.315 (h)(1)",
            "170.315 (h)(2)"));

    /**
     * Starting data for validator.
     */
    public Validator2015Legacy(CertificationCriterionService certificationCriterionService) {

        aaCriteriaOr = Stream.of(certificationCriterionService.get(Criteria2015.G_8),
                certificationCriterionService.get(Criteria2015.G_10)).toList();

        this.counts.put("criteriaRequired", REQUIRED_CRITERIA.size() + CURES_REQUIRED_CRITERIA.size());
        this.counts.put("criteriaRequiredMet", 0);
        this.counts.put("criteriaAaRequired", 1);
        this.counts.put("criteriaAaRequiredMet", 0);
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

    public boolean onValidate() {
        return isCriteriaValid();
    }

    protected boolean isCriteriaValid() {
        this.counts.put("criteriaRequired", REQUIRED_CRITERIA.size() + CURES_REQUIRED_CRITERIA.size());
        boolean criteriaValid = true;
        for (String crit : REQUIRED_CRITERIA) {
            if (criteriaMetContainsCriterion(crit)) {
                this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            } else {
                missingAnd.add(crit);
                criteriaValid = false;
            }
        }
        for (String crit : CURES_REQUIRED_CRITERIA) {
            Boolean foundOriginal = false;
            Boolean foundRevised = false;
            for (CertificationCriterion cert : criteriaMet.keySet()) {
                if (cert.getNumber().equalsIgnoreCase(crit)) {
                    if (cert.getTitle().contains("Cures Update")) {
                        foundRevised = true;
                    } else {
                        foundOriginal = true;
                    }
                }
            }
            if (foundOriginal || foundRevised) {
                this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            } else {
                missingOr.add(new ArrayList<String>(new ArrayList<String>(Arrays.asList(crit,
                        crit + " (Cures Update)"))));
                criteriaValid = false;
            }
        }
        boolean aaValid = isAAValid();
        boolean cpoeValid = isCPOEValid();
        boolean dpValid = isDPValid();

        this.counts.put(
                "criteriaRequired",
                this.counts.get("criteriaRequired") + this.counts.get("criteriaAaRequired")
                + this.counts.get("criteriaCpoeRequired") + this.counts.get("criteriaDpRequired"));
        this.counts.put(
                "criteriaRequiredMet",
                this.counts.get("criteriaRequiredMet") + this.counts.get("criteriaAaRequiredMet")
                + this.counts.get("criteriaCpoeRequiredMet") + this.counts.get("criteriaDpRequiredMet"));

        return (criteriaValid && aaValid && cpoeValid && dpValid);
    }

    protected boolean isAAValid() {
        for (CertificationCriterion crit : aaCriteriaOr) {
            Optional<CertificationCriterion> metAaCriterion = criteriaMet.keySet().stream()
                .filter(criterionMetDto -> criterionMetDto.getId().equals(crit.getId()))
                .findAny();
            if (metAaCriterion.isPresent()) {
                this.counts.put("criteriaAaRequiredMet", 1);
                return true;
            }
        }
        missingOr.add(aaCriteriaOr.stream()
                .map(criterion -> Util.formatCriteriaNumber(criterion))
                .collect(Collectors.toCollection(ArrayList::new)));
        return false;
    }

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

    protected boolean isDPValid() {
        for (String crit : DP_CRITERIA_OR) {
            if (criteriaMetContainsCriterion(crit)) {
                this.counts.put("criteriaDpRequiredMet", 1);
                return true;
            }
        }
        missingOr.add(new ArrayList<String>(DP_CRITERIA_OR));
        return false;
    }

    protected boolean isCqmsValid() {
        return true;
    }

    protected boolean isDomainsValid() {
        return true;
    }
}
