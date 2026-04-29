package gov.healthit.chpl.certificationId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.Util;

public class Validator2025 extends Validator {

    private List<CertificationCriterion> requiredCriteria;
    private List<CertificationCriterion> cpoeCriteriaOr;
    private List<CertificationCriterion> dpCriteriaOr;

    public Validator2025(CertificationCriterionService certificationCriterionService) {

        requiredCriteria = Stream.of(certificationCriterionService.get(Criteria2015.A_5),
                certificationCriterionService.get(Criteria2015.A_14),
                certificationCriterionService.get(Criteria2015.B_1_CURES),
                certificationCriterionService.get(Criteria2015.B_11),
                certificationCriterionService.get(Criteria2015.C_1),
                certificationCriterionService.get(Criteria2015.G_7),
                certificationCriterionService.get(Criteria2015.G_9_CURES),
                certificationCriterionService.get(Criteria2015.G_10)).collect(Collectors.toCollection(ArrayList::new));

        cpoeCriteriaOr = Stream.of(certificationCriterionService.get(Criteria2015.A_1),
                certificationCriterionService.get(Criteria2015.A_2),
                certificationCriterionService.get(Criteria2015.A_3))
                .collect(Collectors.toList());

        dpCriteriaOr = Stream.of(certificationCriterionService.get(Criteria2015.H_1),
                certificationCriterionService.get(Criteria2015.H_2))
                .collect(Collectors.toList());

        this.getCounts().setCriteriaRequired(requiredCriteria.size());
        this.getCounts().setCriteriaRequiredMet(0);
        this.getCounts().setCriteriaCpoeRequired(1);
        this.getCounts().setCriteriaCpoeRequiredMet(0);
        this.getCounts().setCriteriaDpRequired(1);
        this.getCounts().setCriteriaDpRequiredMet(0);
        //Decision support criteria (or "ds") was the a9 or b11 that was required before 2025.
        //Starting with the 2025 calendar year cert ids, b11 is just a required criteria
        this.getCounts().setCriteriaDsRequired(0);
        this.getCounts().setCriteriaDsRequiredMet(0);
        this.getCounts().setCqmsInpatientRequired(0);
        this.getCounts().setCqmsInpatientRequiredMet(0);
        this.getCounts().setCqmsAmbulatoryRequired(0);
        this.getCounts().setCqmsAmbulatoryRequiredMet(0);
        this.getCounts().setCqmsAmbulatoryCoreRequired(0);
        this.getCounts().setCqmsAmbulatoryCoreRequiredMet(0);
        this.getCounts().setDomainsRequired(0);
        this.getCounts().setDomainsRequiredMet(0);
    }

    public boolean onValidate() {
        return isCriteriaValid();
    }

    protected boolean isCriteriaValid() {
        this.getCounts().setCriteriaRequired(requiredCriteria.size());
        boolean requiredCriteriaValid = true;
        for (CertificationCriterion crit : requiredCriteria) {
            Optional<CertificationCriterion> metRequiredCriterion = getCriteriaMet().stream()
                    .filter(criterionMet -> criterionMet.getId().equals(crit.getId()))
                    .findAny();

            if (metRequiredCriterion.isPresent()) {
                this.getCounts().setCriteriaRequiredMet(this.getCounts().getCriteriaRequiredMet() + 1);
            } else {
                this.getMissingAnd().add(Util.formatCriteriaNumber(crit));
                requiredCriteriaValid = false;
            }
        }

        boolean cpoeValid = isCPOEValid();
        boolean dpValid = isDPValid();

        this.getCounts().setCriteriaRequired(
                this.getCounts().getCriteriaRequired()
                + this.getCounts().getCriteriaCpoeRequired()
                + this.getCounts().getCriteriaDsRequired()
                + this.getCounts().getCriteriaDpRequired());
        this.getCounts().setCriteriaRequiredMet(
                this.getCounts().getCriteriaRequiredMet()
                + this.getCounts().getCriteriaCpoeRequiredMet()
                + this.getCounts().getCriteriaDsRequiredMet()
                + this.getCounts().getCriteriaDpRequiredMet());

        return (requiredCriteriaValid && cpoeValid && dpValid);
    }

    protected boolean isCPOEValid() {
        for (CertificationCriterion crit : cpoeCriteriaOr) {
            if (criteriaMetContainsCriterion(crit)) {
                this.getCounts().setCriteriaCpoeRequiredMet(1);
                return true;
            }
        }
        getMissingOr().add(cpoeCriteriaOr.stream()
                .map(cpoeCrit -> Util.formatCriteriaNumber(cpoeCrit))
                .collect(Collectors.toCollection(ArrayList::new)));
        return false;
    }

    protected boolean isDPValid() {
        for (CertificationCriterion crit : dpCriteriaOr) {
            if (criteriaMetContainsCriterion(crit)) {
                this.getCounts().setCriteriaDpRequiredMet(1);
                return true;
            }
        }
        getMissingOr().add(dpCriteriaOr.stream()
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
