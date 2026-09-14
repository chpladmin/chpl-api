package gov.healthit.chpl.certificationId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.certificationId.CertifiedProductDetailsForCertificationId.CertificationResultForCertId;
import gov.healthit.chpl.certifiedproduct.service.CertificationResultUpToDateService;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.util.Util;

public class Validator2026 extends Validator {
    private CertificationResultUpToDateService certResultUpToDateService;
    private CertificationIdYearCalculator certIdYearCalculator;
    private List<CertificationCriterion> requiredCriteria;
    private List<CertificationCriterion> cpoeCriteriaOr;
    private List<CertificationCriterion> dpCriteriaOr;
    private List<CertificationCriterion> upToDateCriteriaFound;

    public Validator2026(CertificationResultUpToDateService certResultUpToDateService,
            CertificationIdYearCalculator certIdYearCalculator,
            CertificationCriterionService certificationCriterionService) {
        this.certResultUpToDateService = certResultUpToDateService;
        this.certIdYearCalculator = certIdYearCalculator;
        upToDateCriteriaFound = new ArrayList<CertificationCriterion>();
        requiredCriteria = Stream.of(certificationCriterionService.get(Criteria2015.A_5),
                certificationCriterionService.get(Criteria2015.A_14),
                certificationCriterionService.get(Criteria2015.B_1_CURES),
                certificationCriterionService.get(Criteria2015.B_11),
                certificationCriterionService.get(Criteria2015.C_1),
                certificationCriterionService.get(Criteria2015.G_7),
                certificationCriterionService.get(Criteria2015.G_9_CURES),
                certificationCriterionService.get(Criteria2015.G_10))
                .collect(Collectors.toCollection(ArrayList::new));

        cpoeCriteriaOr = Stream.of(certificationCriterionService.get(Criteria2015.A_1),
                certificationCriterionService.get(Criteria2015.A_2),
                certificationCriterionService.get(Criteria2015.A_3))
                .collect(Collectors.toList());

        dpCriteriaOr = Stream.of(certificationCriterionService.get(Criteria2015.H_1),
                certificationCriterionService.get(Criteria2015.H_2))
                .collect(Collectors.toList());

        this.getCounts().setCriteriaCpoeRequired(1);
        this.getCounts().setCriteriaCpoeRequiredMet(0);
        this.getCounts().setCriteriaDpRequired(1);
        this.getCounts().setCriteriaDpRequiredMet(0);
        this.getCounts().setCriteriaRequired(requiredCriteria.size() + this.getCounts().getCriteriaDpRequired() + this.getCounts().getCriteriaCpoeRequired());
        this.getCounts().setCriteriaRequiredMet(0);
    }

    public boolean onValidate() {
        //written this way so both validation checks get called and we have all missing info at once
        boolean areAttributesUpToDate = areAttributesUpToDate();
        boolean isCriteriaValid = isCriteriaValid();
        return isCriteriaValid && areAttributesUpToDate;
    }

    protected String getCertIdYear() {
        return "2026";
    }

    protected boolean isCriteriaValid() {
        boolean requiredCriteriaValid = true;
        int requiredCriteriaMet = 0;
        for (CertificationCriterion crit : requiredCriteria) {
            Optional<CertificationCriterion> metRequiredCriterion = getCriteriaMet().stream()
                    .filter(criterionMet -> criterionMet.getId().equals(crit.getId()))
                    .findAny();

            if (metRequiredCriterion.isPresent() && upToDateCriteriaFound.contains(crit)) {
                requiredCriteriaMet++;
            } else if (!metRequiredCriterion.isPresent()) {
                this.getMissingAnd().add(Util.formatCriteriaNumber(crit));
                requiredCriteriaValid = false;
            } else {
                requiredCriteriaValid = false;
            }
        }

        boolean cpoeValid = isCPOEValid();
        boolean dpValid = isDPValid();

        this.getCounts().setCriteriaRequiredMet(
                requiredCriteriaMet
                + this.getCounts().getCriteriaCpoeRequiredMet()
                + this.getCounts().getCriteriaDpRequiredMet());

        return (requiredCriteriaValid && cpoeValid && dpValid);
    }

    protected boolean isCPOEValid() {
        //they could have both "or" criteria, only 1 has to be up-to-date
        List<CertificationCriterion> metCpoeCriteriaOr = cpoeCriteriaOr.stream()
                .filter(orCriterion -> criteriaMetContainsCriterion(orCriterion))
                .collect(Collectors.toList());

        Optional<CertificationCriterion> upToDateMetCpoeCriterionOr = metCpoeCriteriaOr.stream()
            .filter(metOrCriterion -> upToDateCriteriaFound.contains(metOrCriterion))
            .findAny();

        if (!CollectionUtils.isEmpty(metCpoeCriteriaOr) && upToDateMetCpoeCriterionOr.isPresent()) {
            this.getCounts().setCriteriaCpoeRequiredMet(1);
            return true;
        } else if (CollectionUtils.isEmpty(metCpoeCriteriaOr)) {
            getMissingOr().add(cpoeCriteriaOr.stream()
                    .map(cpoeCrit -> Util.formatCriteriaNumber(cpoeCrit))
                    .collect(Collectors.toCollection(ArrayList::new)));
            return false;
        } else {
            return false;
        }
    }

    protected boolean isDPValid() {
        List<CertificationCriterion> metDpCriteriaOr = dpCriteriaOr.stream()
                .filter(orCriterion -> criteriaMetContainsCriterion(orCriterion))
                .collect(Collectors.toList());

        Optional<CertificationCriterion> upToDateMetDpCriterionOr = metDpCriteriaOr.stream()
            .filter(metOrCriterion -> upToDateCriteriaFound.contains(metOrCriterion))
            .findAny();

        if (!CollectionUtils.isEmpty(metDpCriteriaOr) && upToDateMetDpCriterionOr.isPresent()) {
            this.getCounts().setCriteriaDpRequiredMet(1);
            return true;
        } else if (CollectionUtils.isEmpty(metDpCriteriaOr)) {
            getMissingOr().add(dpCriteriaOr.stream()
                    .map(dpCrit -> Util.formatCriteriaNumber(dpCrit))
                    .collect(Collectors.toCollection(ArrayList::new)));
            return false;
        } else {
            return false;
        }
    }

    @Override
    protected void calculatePercentages() {
        getPercents().setCriteriaMet(getCounts().getCriteriaRequired() == 0
                ? 0
                : Math.min((int) Math.floor((getCounts().getCriteriaRequiredMet() * 100.0)
                        / getCounts().getCriteriaRequired()), 100));
        getPercents().setCqmDomains(0);
        getPercents().setCqmsInpatient(0);
        getPercents().setCqmsAmbulatory(0);
    }

    private boolean areAttributesUpToDate() {
        //Get the date on which to determine which attributes are required.
        //Ex: on 9/1/2026 see whichever standards and code sets were required
        //and then we need to confirm the listings being used for cert id creation
        //have those attributes today.
        LocalDate dayToCalculateRequiredAttributes = certIdYearCalculator.getCmsIdStartDayOfYear(getCertIdYear());

        List<CertificationCriterion> criteriaToCheckForUpdates = Stream.of(requiredCriteria, cpoeCriteriaOr, dpCriteriaOr)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        criteriaToCheckForUpdates.stream()
            .forEach(criterion -> {
                //any one cert result for the criterion being checked must be fully up-to-date
                List<CertificationResultForCertId> certResults = this.getListings().stream()
                        .flatMap(listing -> listing.getCertificationResults().stream())
                        .filter(certResult -> certResult.getCertificationCriterion().getId().equals(criterion.getId()))
                        .collect(Collectors.toList());

                if (!CollectionUtils.isEmpty(certResults)) {
                    CertificationResultForCertId fullyUpToDateCertResultForCriterion = certResults.stream()
                        .filter(certResult -> certResultUpToDateService.isUpToDate(certResult.getCertResultId(), dayToCalculateRequiredAttributes))
                        .findAny()
                        .orElse(null);

                    if (fullyUpToDateCertResultForCriterion == null) {
                        getMissingUpToDate().add(Util.formatCriteriaNumber(criterion));
                    } else {
                        this.getCounts().setCriteriaUpToDateMet(this.getCounts().getCriteriaUpToDateMet() + 1);
                        upToDateCriteriaFound.add(criterion);
                    }
                }
            });
        return CollectionUtils.isEmpty(getMissingUpToDate());
    }

    protected boolean isCqmsValid() {
        return true;
    }

    protected boolean isDomainsValid() {
        return true;
    }
}
