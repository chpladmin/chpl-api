package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMResultCertification;
import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("cqmAttestedCriteriaReviewer")
public class CqmAttestedCriteriaReviewer implements Reviewer {
    private CertificationCriterionService criteriaService;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public CqmAttestedCriteriaReviewer(CertificationCriterionService criteriaService,
            ValidationUtils validationUtils, ErrorMessageUtil msgUtil) {
        this.criteriaService = criteriaService;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (CollectionUtils.isEmpty(listing.getCqmResults())) {
            return;
        }

        List<CertificationCriterion> attestedCriteria = validationUtils.getAttestedCriteria(listing);
        listing.getCqmResults().stream()
                .filter(cqmResult -> BooleanUtils.isTrue(cqmResult.isSuccess()))
                .forEach(cqmResult -> removeValuesThatAreNotCriteria(cqmResult, listing));

        listing.getCqmResults().stream()
                .filter(cqmResult -> BooleanUtils.isTrue(cqmResult.isSuccess()))
                .forEach(cqmResult -> reviewListingHasAllCriteriaForCqmResult(cqmResult, listing, attestedCriteria));
    }

    private void removeValuesThatAreNotCriteria(CQMResultDetails cqm, CertifiedProductSearchDetails listing) {
        List<CQMResultCertification> cqmCriteriaToRemove = cqm.getCriteria().stream()
                .filter(cqmCriterion -> !criteriaService.isCriteriaNumber(cqmCriterion.getCertificationNumber()))
                .toList();
        cqmCriteriaToRemove.stream()
                .forEach(cqmCriterion -> removeAssociatedCriterion(cqm, cqmCriterion, listing));
    }

    private void removeAssociatedCriterion(CQMResultDetails cqm, CQMResultCertification cqmCriterionToRemove,
            CertifiedProductSearchDetails listing) {
        cqm.getCriteria().remove(cqmCriterionToRemove);
        listing.addWarningMessage(msgUtil.getMessage("listing.criteria.removedCriteriaForCqm",
                cqmCriterionToRemove.getCertificationNumber(),
                cqm.getCmsId()));
    }

    private void reviewListingHasAllCriteriaForCqmResult(CQMResultDetails cqm,
            CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        cqm.getCriteria().stream()
                .forEach(cqmCriterion -> reviewListingHasCqmCriterion(cqm, cqmCriterion, listing, attestedCriteria));
    }

    private void reviewListingHasCqmCriterion(CQMResultDetails cqm, CQMResultCertification cqmCriterion,
            CertifiedProductSearchDetails listing, List<CertificationCriterion> attestedCriteria) {
        CertificationCriterion criterion = criteriaService.get(cqmCriterion.getCertificationId());
        if (criterion == null) {
            listing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.criteria.missingCriteriaForCqm",
                            cqm.getCmsId(), cqmCriterion.getCertificationNumber()));
        } else if (cqmCriterion.getCriterion() != null
                && !validationUtils.hasCriterion(criterion, attestedCriteria)) {
            listing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.criteria.missingCriteriaForCqm",
                            cqm.getCmsId(), Util.formatCriteriaNumber(cqmCriterion.getCriterion())));
        } else if (cqmCriterion.getCriterion() == null
                && !validationUtils.hasCriterion(criterion, attestedCriteria)) {
            listing.addBusinessErrorMessage(
                    msgUtil.getMessage("listing.criteria.missingCriteriaForCqm",
                            cqm.getCmsId(), cqmCriterion.getCertificationNumber()));
        }
    }
}
