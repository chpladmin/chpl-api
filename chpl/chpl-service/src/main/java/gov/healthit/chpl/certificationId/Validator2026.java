package gov.healthit.chpl.certificationId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;

import gov.healthit.chpl.attribute.CodeSetsUpToDateService;
import gov.healthit.chpl.attribute.GroupedStandardsUpToDateService;
import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.standard.BaselineStandardService;
import gov.healthit.chpl.util.Util;

public class Validator2026 extends Validator {
    private static final int NUM_CRITERIA_REQUIRING_UPDATES = 4; //a5, b1, g9, g10
    private CertificationCriterionService certificationCriterionService;
    private CertificationIdYearCalculator certIdYearCalculator;
    private BaselineStandardService baselineStandardService;
    private GroupedStandardsUpToDateService groupedStandardService;
    private CodeSetsUpToDateService codeSetService;

    private List<CertificationCriterion> requiredCriteria;
    private List<CertificationCriterion> cpoeCriteriaOr;
    private List<CertificationCriterion> dpCriteriaOr;
    private List<CertificationCriterion> criteriaToCheckForUpdates;
    private CertificationCriterion a5, b1, g9, g10;

    public Validator2026(CertificationCriterionService certificationCriterionService,
            CertificationIdYearCalculator certIdYearCalculator,
            BaselineStandardService baselineStandardService,
            GroupedStandardsUpToDateService groupedStandardService,
            CodeSetsUpToDateService codeSetService) {
        this.certificationCriterionService = certificationCriterionService;
        this.certIdYearCalculator = certIdYearCalculator;
        this.baselineStandardService = baselineStandardService;
        this.groupedStandardService = groupedStandardService;
        this.codeSetService = codeSetService;

        a5 = certificationCriterionService.get(Criteria2015.A_5);
        b1 = certificationCriterionService.get(Criteria2015.B_1_CURES);
        g9 = certificationCriterionService.get(Criteria2015.G_9_CURES);
        g10 = certificationCriterionService.get(Criteria2015.G_10);
        criteriaToCheckForUpdates = Stream.of(a5, b1, g9, g10).toList();

        requiredCriteria = Stream.of(a5,
                certificationCriterionService.get(Criteria2015.A_14),
                b1,
                certificationCriterionService.get(Criteria2015.B_11),
                certificationCriterionService.get(Criteria2015.C_1),
                certificationCriterionService.get(Criteria2015.G_7),
                g9,
                g10).collect(Collectors.toCollection(ArrayList::new));

        cpoeCriteriaOr = Stream.of(certificationCriterionService.get(Criteria2015.A_1),
                certificationCriterionService.get(Criteria2015.A_2),
                certificationCriterionService.get(Criteria2015.A_3))
                .collect(Collectors.toList());

        dpCriteriaOr = Stream.of(certificationCriterionService.get(Criteria2015.H_1),
                certificationCriterionService.get(Criteria2015.H_2))
                .collect(Collectors.toList());

        this.getCounts().setCriteriaRequired(requiredCriteria.size());
        this.getCounts().setCriteriaRequiredMet(0);
        this.getCounts().setCriteriaUpToDateRequired(NUM_CRITERIA_REQUIRING_UPDATES);
        this.getCounts().setCriteriaUpToDateMet(0);
        this.getCounts().setCriteriaCpoeRequired(1);
        this.getCounts().setCriteriaCpoeRequiredMet(0);
        this.getCounts().setCriteriaDpRequired(1);
        this.getCounts().setCriteriaDpRequiredMet(0);
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
        return isCriteriaValid() && areAttributesUpToDate();
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

    private boolean areAttributesUpToDate() {
        //Get the date on which to determine which attributes are required.
        //Ex: on 9/1/2026 see whichever standards and code sets were required
        //and then we need to confirm the listings being used for cert id creation
        //have those attributes today.
        LocalDate dayToCalculateRequiredAttributes = certIdYearCalculator.getCmsIdStartDayOfCurrentYear();

        List<CertificationCriterion> criteriaNotUpToDate = new ArrayList<CertificationCriterion>();
        criteriaToCheckForUpdates.stream()
            .forEach(criterion -> {
                boolean upToDate = true;
                upToDate = upToDate && areBaselineStandardsUpToDateForCriterion(criterion, dayToCalculateRequiredAttributes);
                upToDate = upToDate && areGroupedStandardsUpToDateForCriterion(criterion, dayToCalculateRequiredAttributes);
                upToDate = upToDate && areCodeSetsUpToDateForCriterion(criterion, dayToCalculateRequiredAttributes);
                if (!upToDate) {
                    criteriaNotUpToDate.add(criterion);
                } else {
                    this.getCounts().setCriteriaUpToDateMet(this.getCounts().getCriteriaUpToDateMet() + 1);
                }
            });
        return CollectionUtils.isEmpty(criteriaNotUpToDate);
    }

    private boolean areBaselineStandardsUpToDateForCriterion(CertificationCriterion criterion, LocalDate asOfDate) {

    }

    private boolean areGroupedStandardsUpToDateForCriterion(CertificationCriterion criterion, LocalDate asOfDate) {

    }

    private boolean areCodeSetsUpToDateForCriterion(CertificationCriterion criterion, LocalDate asOfDate) {
        //what code sets were required on the date
        codeSetse
    }

    protected boolean isCqmsValid() {
        return true;
    }

    protected boolean isDomainsValid() {
        return true;
    }
}
