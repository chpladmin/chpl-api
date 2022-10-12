package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("ucdProcessReviewer")
public class UcdProcessReviewer implements Reviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;
    private List<CertificationCriterion> ucdProcessCriteria = new ArrayList<CertificationCriterion>();

    @Autowired
    public UcdProcessReviewer(CertificationCriterionService criterionService,
            ValidationUtils validationUtils,
            CertificationResultRules certResultRules,
            ErrorMessageUtil msgUtil,
            @Value("${sedCriteria}") String ucdProcessCriteria) {
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;

        this.ucdProcessCriteria = Arrays.asList(ucdProcessCriteria.split(",")).stream()
                .map(id -> criterionService.get(Long.parseLong(id)))
                .collect(Collectors.toList());
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getSed() == null) {
            return;
        }
        removeUcdProcessesNotFound(listing);
        reviewAllUcdProcessCriteriaAreAllowed(listing);
        reviewCertResultsHaveUcdProcessesIfRequired(listing);
        addFuzzyMatchWarnings(listing);
    }

    private void removeUcdProcessesNotFound(CertifiedProductSearchDetails listing) {
        List<CertifiedProductUcdProcess> ucdProcesses = listing.getSed().getUcdProcesses();
        if (!CollectionUtils.isEmpty(ucdProcesses)) {
            List<CertifiedProductUcdProcess> ucdProcessesWithoutFuzzyMatchesOrIds = ucdProcesses.stream()
                    .filter(currUcdProc -> StringUtils.isEmpty(currUcdProc.getUserEnteredName()))
                    .filter(currUcdProc -> currUcdProc.getId() == null)
                    .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(ucdProcessesWithoutFuzzyMatchesOrIds)) {
                ucdProcesses.removeAll(ucdProcessesWithoutFuzzyMatchesOrIds);

                ucdProcessesWithoutFuzzyMatchesOrIds.stream()
                    .forEach(ucdProcWithoutId -> listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.ucdProcessNotFoundAndRemoved",
                                    ucdProcWithoutId.getName(),
                                    ucdProcWithoutId.getCriteria().stream()
                                        .map(criterion -> Util.formatCriteriaNumber(criterion))
                                        .collect(Collectors.joining(",")))));
            }
        }
    }

    private void reviewAllUcdProcessCriteriaAreAllowed(CertifiedProductSearchDetails listing) {
        if (listing.getSed() != null && !CollectionUtils.isEmpty(listing.getSed().getUcdProcesses())) {
            listing.getSed().getUcdProcesses().stream()
                .filter(ucdProcess -> !CollectionUtils.isEmpty(ucdProcess.getCriteria()))
                .flatMap(ucdProcess -> ucdProcess.getCriteria().stream())
                .filter(ucdCriterion -> !certResultRules.hasCertOption(ucdCriterion.getNumber(), CertificationResultRules.UCD_FIELDS))
                .filter(ucdCriterion -> BooleanUtils.isFalse(ucdCriterion.getRemoved()))
                .forEach(notAllowedUcdCriterion ->
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.ucdProcessNotApplicable", Util.formatCriteriaNumber(notAllowedUcdCriterion))));
        }
    }

    private void reviewCertResultsHaveUcdProcessesIfRequired(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        ucdProcessCriteria.stream()
            .filter(criterion -> validationUtils.hasCriterion(criterion, attestedCriteria))
            .map(attestedUcdProcessCriterion -> getCertificationResultForCriterion(listing, attestedUcdProcessCriterion))
            .filter(certResult -> certResult != null && validationUtils.isEligibleForErrors(certResult))
            .forEach(certResult -> reviewCertResultHasUcdProcessIfRequired(listing, certResult));
    }

    private CertificationResult getCertificationResultForCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterionToReview) {
        Optional<CertificationResult> certResultToReviewOpt = listing.getCertificationResults().stream()
                .filter(certResult -> certResult.getCriterion().getId().equals(criterionToReview.getId()))
                .findAny();
        if (certResultToReviewOpt.isPresent()) {
            return certResultToReviewOpt.get();
        }
        return null;
    }

    private void reviewCertResultHasUcdProcessIfRequired(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.isSed()) {
            if (listing.getSed() == null || CollectionUtils.isEmpty(listing.getSed().getUcdProcesses())) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingUcdProcess",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            } else if (!doesUcdProcessListContainCriterion(listing, certResult.getCriterion())) {
                listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.missingUcdProcess",
                        Util.formatCriteriaNumber(certResult.getCriterion())));
            }
        }
    }

    private boolean doesUcdProcessListContainCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getSed().getUcdProcesses().stream()
            .flatMap(ucdProcess -> ucdProcess.getCriteria().stream())
            .filter(ucdProcessCriterion -> ucdProcessCriterion.getId().equals(criterion.getId()))
            .count() > 0;
    }

    private void addFuzzyMatchWarnings(CertifiedProductSearchDetails listing) {
        if (!CollectionUtils.isEmpty(listing.getSed().getUcdProcesses())) {
            listing.getSed().getUcdProcesses().stream()
                .filter(ucdProcess -> hasFuzzyMatch(ucdProcess))
                .forEach(ucdProcess -> addFuzzyMatchWarning(listing, ucdProcess));
        }
    }

    private boolean hasFuzzyMatch(CertifiedProductUcdProcess ucdProcess) {
        return ucdProcess.getId() == null
                && !StringUtils.isEmpty(ucdProcess.getName())
                && !StringUtils.equals(ucdProcess.getName(), ucdProcess.getUserEnteredName());
    }

    private void addFuzzyMatchWarning(CertifiedProductSearchDetails listing, CertifiedProductUcdProcess ucdProcess) {
        String warningMsg = msgUtil.getMessage("listing.fuzzyMatch", FuzzyType.UCD_PROCESS.fuzzyType(),
                ucdProcess.getUserEnteredName(), ucdProcess.getName());
        listing.getWarningMessages().add(warningMsg);
    }
}
