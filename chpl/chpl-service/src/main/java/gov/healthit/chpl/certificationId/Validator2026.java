package gov.healthit.chpl.certificationId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.codeset.CodeSetDAO;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.service.CertificationCriterionService.Criteria2015;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardCriteriaMap;
import gov.healthit.chpl.standard.StandardDAO;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.Util;

public class Validator2026 extends Validator {
    private static final int NUM_CRITERIA_REQUIRING_UPDATES = 4; //a5, b1, g9, g10
    private CertificationIdYearCalculator certIdYearCalculator;
    private StandardDAO standardDao;
    private CodeSetDAO codeSetDao;

    private List<CertificationCriterion> requiredCriteria;
    private List<CertificationCriterion> cpoeCriteriaOr;
    private List<CertificationCriterion> dpCriteriaOr;
    private List<CertificationCriterion> criteriaToCheckForUpdates;
    private CertificationCriterion a5, b1, g9, g10;

    public Validator2026(CertificationCriterionService certificationCriterionService,
            CertificationIdYearCalculator certIdYearCalculator,
            StandardDAO standardDao,
            CodeSetDAO codeSetDao) {
        this.certIdYearCalculator = certIdYearCalculator;
        this.standardDao = standardDao;
        this.codeSetDao = codeSetDao;

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
        //written this way so both validation checks get called and we have all missing info at once
        boolean isCriteriaValid = isCriteriaValid();
        boolean areAttributesUpToDate = areAttributesUpToDate();
        return isCriteriaValid && areAttributesUpToDate;
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

        criteriaToCheckForUpdates.stream()
            .forEach(criterion -> {
                //any one cert result for the criterion being checked must be fully up-to-date
                List<CertificationResult> certResultsForCriterion = this.getListings().stream()
                        .flatMap(listing -> listing.getCertificationResults().stream())
                        .filter(certResult -> certResult.getCriterion().getId().equals(criterion.getId()) && BooleanUtils.isTrue(certResult.getSuccess()))
                        .collect(Collectors.toList());

                if (!CollectionUtils.isEmpty(certResultsForCriterion)) {
                    CertificationResult fullyUpToDateCertResultForCriterion = certResultsForCriterion.stream()
                        .filter(certResult -> isCertResultFullyUpToDate(certResult, dayToCalculateRequiredAttributes))
                        .findAny()
                        .orElse(null);

                    if (fullyUpToDateCertResultForCriterion == null) {
                        getMissingUpToDate().add(Util.formatCriteriaNumber(criterion));
                    } else {
                        this.getCounts().setCriteriaUpToDateMet(this.getCounts().getCriteriaUpToDateMet() + 1);
                    }
                }
            });
        return CollectionUtils.isEmpty(getMissingUpToDate());
    }

    private boolean isCertResultFullyUpToDate(CertificationResult certResult, LocalDate asOfDate) {
        return areStandardsUpToDate(certResult, asOfDate)
                && areCodeSetsUpToDate(certResult, asOfDate);
    }

    private boolean areStandardsUpToDate(CertificationResult certResult, LocalDate asOfDate) {
        List<StandardCriteriaMap> stdCriteriaMaps = standardDao.getAllStandardCriteriaMap();
        stdCriteriaMaps.removeIf(map -> !map.getCriterion().getId().equals(certResult.getCriterion().getId()));
        List<Standard> requiredStandardsForCriterionAsOfDate = stdCriteriaMaps.stream()
                .map(map -> map.getStandard())
                .filter(std -> std.getStartDay().isBefore(asOfDate)
                        && (std.getRequiredDay() != null && DateUtil.isOnOrBefore(std.getRequiredDay(), asOfDate))
                        && (std.getEndDay() == null || std.getEndDay().isAfter(asOfDate)))
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(requiredStandardsForCriterionAsOfDate)) {
            return !requiredStandardsForCriterionAsOfDate.stream()
                    .filter(requiredStandard -> !isStandardOnCertResult(requiredStandard, certResult))
                    .findAny()
                    .isPresent();
        }
        return true;
    }

    private boolean isStandardOnCertResult(Standard standard, CertificationResult certResult) {
        return certResult.getStandards().stream()
                .filter(certResultStd -> certResultStd.getStandard().getId().equals(standard.getId()))
                .findAny()
                .isPresent();
    }

    private boolean areCodeSetsUpToDate(CertificationResult certResult, LocalDate asOfDate) {
        List<CodeSet> codeSetsRequiredForCriterion = null;
        Map<Long, List<CodeSet>> codeSetMaps = codeSetDao.getCodeSetCriteriaMaps();
        if (codeSetMaps.containsKey(certResult.getCriterion().getId())) {
            codeSetsRequiredForCriterion = codeSetMaps.get(certResult.getCriterion().getId()).stream()
                    .filter(codeSet -> DateUtil.isOnOrBefore(codeSet.getRequiredDay(), asOfDate))
                    .collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(codeSetsRequiredForCriterion)) {
                return !codeSetsRequiredForCriterion.stream()
                        .filter(requiredCodeSet -> !isCodeSetOnCertResult(requiredCodeSet, certResult))
                        .findAny()
                        .isPresent();
            }
        }
        return true;
    }

    private Boolean isCodeSetOnCertResult(CodeSet codeSet, CertificationResult certResult) {
        return certResult.getCodeSets().stream()
                .filter(cs -> cs.getCodeSet().getId().equals(codeSet.getId()))
                .findAny()
                .isPresent();
    }

    protected boolean isCqmsValid() {
        return true;
    }

    protected boolean isDomainsValid() {
        return true;
    }
}
