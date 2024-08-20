package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.ff4j.FF4j;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.Util;

/**
 * Validator for CMS EHR ID generation for 2015 Edition, post Cures rule.
 * @author alarned
 *
 */
public class Validator2015 extends Validator {

    private List<CertificationCriterion> requiredCriteria;
    private List<CertificationCriterion> cpoeCriteriaOr;
    private List<CertificationCriterion> decisionSupportRequiredCriteriaOr;
    private List<CertificationCriterion> dpCriteriaOr;

    public Validator2015(CertificationCriterionService certificationCriterionService,
            FF4j ff4j) {

        CertificationCriterion a9 = certificationCriterionService.get(Criteria2015.A_9);
        CertificationCriterion b11 = certificationCriterionService.get(Criteria2015.B_11);

        requiredCriteria = Stream.of(certificationCriterionService.get(Criteria2015.A_5),
                certificationCriterionService.get(Criteria2015.A_14),
                certificationCriterionService.get(Criteria2015.B_1_CURES),
                certificationCriterionService.get(Criteria2015.C_1),
                certificationCriterionService.get(Criteria2015.G_7),
                certificationCriterionService.get(Criteria2015.G_9_CURES),
                certificationCriterionService.get(Criteria2015.G_10)).collect(Collectors.toCollection(ArrayList::new));

        cpoeCriteriaOr = Stream.of(certificationCriterionService.get(Criteria2015.A_1),
                certificationCriterionService.get(Criteria2015.A_2),
                certificationCriterionService.get(Criteria2015.A_3))
                .collect(Collectors.toList());

        if (ff4j.check(FeatureList.CMS_A9_GRACE_PERIOD_END)) {
            requiredCriteria.add(3, b11); //adding at index 3 so the criteria appear in order to the user
            decisionSupportRequiredCriteriaOr = new ArrayList<CertificationCriterion>();
        } else {
            decisionSupportRequiredCriteriaOr = Stream.of(a9, b11).toList();
        }

        dpCriteriaOr = Stream.of(certificationCriterionService.get(Criteria2015.H_1),
                certificationCriterionService.get(Criteria2015.H_2))
                .collect(Collectors.toList());

        this.counts.put("criteriaRequired", requiredCriteria.size());
        this.counts.put("criteriaRequiredMet", 0);
        this.counts.put("criteriaCpoeRequired", 1);
        this.counts.put("criteriaCpoeRequiredMet", 0);
        this.counts.put("criteriaDsRequired", 1);
        this.counts.put("criteriaDsRequiredMet", 0);
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
        this.counts.put("criteriaRequired", requiredCriteria.size());
        boolean requiredCriteriaValid = true;
        for (CertificationCriterion crit : requiredCriteria) {
            Optional<CertificationCriterion> metRequiredCriterion = criteriaMet.keySet().stream()
                    .filter(criterionMet -> criterionMet.getId().equals(crit.getId()))
                    .findAny();

            if (metRequiredCriterion.isPresent()) {
                this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            } else {
                missingAnd.add(Util.formatCriteriaNumber(crit));
                requiredCriteriaValid = false;
            }
        }

        boolean cpoeValid = isCPOEValid();
        boolean dsValid = isDecisionSupportValid();
        boolean dpValid = isDPValid();

        this.counts.put("criteriaRequired",
                this.counts.get("criteriaRequired")
                + this.counts.get("criteriaCpoeRequired")
                + this.counts.get("criteriaDsRequired")
                + this.counts.get("criteriaDpRequired"));
        this.counts.put("criteriaRequiredMet",
                this.counts.get("criteriaRequiredMet")
                + this.counts.get("criteriaCpoeRequiredMet")
                + this.counts.get("criteriaDsRequiredMet")
                + this.counts.get("criteriaDpRequiredMet"));

        return (requiredCriteriaValid && cpoeValid && dsValid && dpValid);
    }

    protected boolean isCPOEValid() {
        for (CertificationCriterion crit : cpoeCriteriaOr) {
            if (criteriaMetContainsCriterion(crit)) {
                this.counts.put("criteriaCpoeRequiredMet", 1);
                return true;
            }
        }
        missingOr.add(cpoeCriteriaOr.stream()
                .map(cpoeCrit -> Util.formatCriteriaNumber(cpoeCrit))
                .collect(Collectors.toCollection(ArrayList::new)));
        return false;
    }

    protected boolean isDecisionSupportValid() {
    	if (decisionSupportRequiredCriteriaOr.isEmpty()) {
            this.counts.put("criteriaDsRequiredMet", 1);
            return true;
    	}
        for (CertificationCriterion crit : decisionSupportRequiredCriteriaOr) {
            if (criteriaMetContainsCriterion(crit)) {
                this.counts.put("criteriaDsRequiredMet", 1);
                return true;
            }
        }
        missingOr.add(decisionSupportRequiredCriteriaOr.stream()
                .map(dsCrit -> Util.formatCriteriaNumber(dsCrit))
                .collect(Collectors.toCollection(ArrayList::new)));
        return false;
    }

    protected boolean isDPValid() {
        for (CertificationCriterion crit : dpCriteriaOr) {
            if (criteriaMetContainsCriterion(crit)) {
                this.counts.put("criteriaDpRequiredMet", 1);
                return true;
            }
        }
        missingOr.add(dpCriteriaOr.stream()
                .map(dpCrit -> Util.formatCriteriaNumber(dpCrit))
                .collect(Collectors.toCollection(ArrayList::new)));
        return false;
    }

    protected boolean isCqmsValid() {
        return true;
    }

    protected boolean isDomainsValid() {
        return true;
    }
}
