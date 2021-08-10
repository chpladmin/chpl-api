package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.PermissionBasedReviewer;

@Component("listingUploadUcdProcessReviewer")
public class UcdProcessReviewer extends PermissionBasedReviewer {
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private List<CertificationCriterion> ucdProcessCriteria = new ArrayList<CertificationCriterion>();

    @Autowired
    public UcdProcessReviewer(CertificationCriterionService criterionService,
            ValidationUtils validationUtils,
            CertificationResultRules certResultRules,
            @Value("${sedCriteria}") String ucdProcessCriteria,
            ErrorMessageUtil errorMessageUtil, ResourcePermissions resourcePermissions) {
        super(errorMessageUtil, resourcePermissions);
        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;

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
        reviewUcdFields(listing);
    }

    private void removeUcdProcessesNotFound(CertifiedProductSearchDetails listing) {
        List<UcdProcess> ucdProcesses = listing.getSed().getUcdProcesses();
        if (!CollectionUtils.isEmpty(ucdProcesses)) {
            List<UcdProcess> ucdProcessesWithoutIds = ucdProcesses.stream()
                        .filter(currUcdProc -> currUcdProc.getId() == null)
                        .collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(ucdProcessesWithoutIds)) {
                ucdProcesses.removeAll(ucdProcessesWithoutIds);

                ucdProcessesWithoutIds.stream()
                    .forEach(ucdProcWithoutId -> listing.getWarningMessages().add(
                            msgUtil.getMessage("listing.criteria.ucdProcessNotFoundAndRemoved", ucdProcWithoutId.getName(),
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
                .forEach(notAllowedUcdCriterion ->
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.ucdProcessNotApplicable", Util.formatCriteriaNumber(notAllowedUcdCriterion))));

            listing.getSed().getUcdProcesses().stream()
                .filter(ucdProcess -> !CollectionUtils.isEmpty(ucdProcess.getCriteria()))
                .flatMap(ucdProcess -> ucdProcess.getCriteria().stream())
                .filter(ucdCriterion -> !doesListingAttestToCriterion(listing, ucdCriterion))
                .forEach(notAllowedUcdCriterion ->
                    listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.ucdProcessNotApplicable", Util.formatCriteriaNumber(notAllowedUcdCriterion))));
        }
    }

    public void reviewUcdFields(CertifiedProductSearchDetails listing) {
        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        ucdProcessCriteria.stream()
            .filter(criterion -> validationUtils.hasCriterion(criterion, attestedCriteria))
            .map(attestedUcdProcessCriterion -> getCertificationResultForCriterion(listing, attestedUcdProcessCriterion))
            .filter(certResult -> certResult != null)
            .forEach(certResult -> reviewUcdFields(listing, certResult));
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

    private void reviewUcdFields(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (certResult.isSed()) {
            if (listing.getSed() == null || CollectionUtils.isEmpty(listing.getSed().getUcdProcesses())) {
                addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingUcdProcess",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            } else if (!doesUcdProcessListContainCriterion(listing, certResult.getCriterion())) {
                addCriterionErrorOrWarningByPermission(listing, certResult, "listing.criteria.missingUcdProcess",
                        Util.formatCriteriaNumber(certResult.getCriterion()));
            }
        }
    }

    private boolean doesUcdProcessListContainCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getSed().getUcdProcesses().stream()
            .flatMap(ucdProcess -> ucdProcess.getCriteria().stream())
            .filter(ucdProcessCriterion -> ucdProcessCriterion.getId().equals(criterion.getId()))
            .count() > 0;
    }

    private boolean doesListingAttestToCriterion(CertifiedProductSearchDetails listing, CertificationCriterion criterion) {
        return listing.getCertificationResults().stream()
            .filter(certResult -> certResult.getCriterion() != null && BooleanUtils.isTrue(certResult.isSuccess())
                && certResult.getCriterion().getId().equals(criterion.getId()))
            .count() > 0;
    }
}
