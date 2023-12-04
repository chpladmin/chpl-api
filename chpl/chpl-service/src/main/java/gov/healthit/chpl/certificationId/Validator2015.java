package gov.healthit.chpl.certificationId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.CollectionUtils;

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
    private List<CertificationCriterion> baseRequiredCriteriaOr;

    protected static final List<String> CPOE_CRITERIA_OR = new ArrayList<String>(Arrays.asList("170.315 (a)(1)",
            "170.315 (a)(2)", "170.315 (a)(3)"));

    protected static final List<String> DP_CRITERIA_OR = new ArrayList<String>(Arrays.asList("170.315 (h)(1)",
            "170.315 (h)(2)"));


    public Validator2015(CertificationCriterionService certificationCriterionService) {
        requiredCriteria = Stream.of(certificationCriterionService.get(Criteria2015.A_5),
                certificationCriterionService.get(Criteria2015.A_14),
                certificationCriterionService.get(Criteria2015.B_1_CURES),
                certificationCriterionService.get(Criteria2015.C_1),
                certificationCriterionService.get(Criteria2015.G_7),
                certificationCriterionService.get(Criteria2015.G_9_CURES),
                certificationCriterionService.get(Criteria2015.G_10)).collect(Collectors.toCollection(ArrayList::new));

        CertificationCriterion a9 = certificationCriterionService.get(Criteria2015.A_9);
        CertificationCriterion b11 = certificationCriterionService.get(Criteria2015.B_11);
        if (isCriteriaAvailable(b11) && isCriteriaAvailable(a9)) {
            baseRequiredCriteriaOr = new ArrayList<CertificationCriterion>();
            baseRequiredCriteriaOr.add(a9);
            baseRequiredCriteriaOr.add(b11);
            this.counts.put("criteriaBaseRequired", 1);
            this.counts.put("criteriaBaseRequiredMet", 0);
        } else if (!isCriteriaAvailable(b11) && isCriteriaAvailable(a9)) {
            requiredCriteria.add(a9);
            baseRequiredCriteriaOr = new ArrayList<CertificationCriterion>();
            this.counts.put("criteriaBaseRequired", 0);
            this.counts.put("criteriaBaseRequiredMet", 0);
        } else if (isCriteriaAvailable(b11) && !isCriteriaAvailable(a9)) {
            requiredCriteria.add(b11);
            baseRequiredCriteriaOr = new ArrayList<CertificationCriterion>();
            this.counts.put("criteriaBaseRequired", 0);
            this.counts.put("criteriaBaseRequiredMet", 0);
        } else {
            //neither are available
            baseRequiredCriteriaOr = new ArrayList<CertificationCriterion>();
            this.counts.put("criteriaBaseRequired", 0);
            this.counts.put("criteriaBaseRequiredMet", 0);
        }

        this.counts.put("criteriaRequired", requiredCriteria.size());
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

    public boolean onValidate() {
        return isCriteriaValid();
    }

    protected boolean isCriteriaValid() {
        this.counts.put("criteriaRequired", requiredCriteria.size());
        boolean criteriaValid = true;
        for (CertificationCriterion crit : requiredCriteria) {
            Optional<CertificationCriterion> metRequiredCriterion = criteriaMet.keySet().stream()
                    .filter(criterionMet -> criterionMet.getId().equals(crit.getId()))
                    .findAny();

            if (metRequiredCriterion.isPresent()) {
                this.counts.put("criteriaRequiredMet", this.counts.get("criteriaRequiredMet") + 1);
            } else {
                missingAnd.add(Util.formatCriteriaNumber(crit));
                criteriaValid = false;
            }
        }

        boolean isBaseValid = isBaseValid();
        boolean cpoeValid = isCPOEValid();
        boolean dpValid = isDPValid();

        this.counts.put("criteriaRequired",
                this.counts.get("criteriaRequired")
                + this.counts.get("criteriaCpoeRequired")
                + this.counts.get("criteriaDpRequired")
                + this.counts.get("criteriaBaseRequired"));
        this.counts.put("criteriaRequiredMet",
                this.counts.get("criteriaRequiredMet")
                + this.counts.get("criteriaCpoeRequiredMet")
                + this.counts.get("criteriaDpRequiredMet")
                + this.counts.get("criteriaBaseRequiredMet"));

        return (criteriaValid && cpoeValid && dpValid && isBaseValid);
    }

    protected boolean isBaseValid() {
        if (CollectionUtils.isEmpty(baseRequiredCriteriaOr)) {
            return true;
        } else {
            for (CertificationCriterion crit : baseRequiredCriteriaOr) {
                if (criteriaMetContainsCriterion(crit)) {
                    this.counts.put("criteriaBaseRequiredMet", 1);
                    return true;
                }
            }
            missingOr.add(baseRequiredCriteriaOr.stream()
                    .map(crit -> Util.formatCriteriaNumber(crit))
                    .collect(Collectors.toCollection(ArrayList::new)));
            return false;
        }
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

    private boolean isCriteriaAvailable(CertificationCriterion criterion) {
        LocalDate today = LocalDate.now();
        return (criterion.getStartDay() != null
                    && (criterion.getStartDay().isEqual(today) || criterion.getStartDay().isBefore(today)))
                && (criterion.getEndDay() == null || criterion.getEndDay().isAfter(today));
    }
}
