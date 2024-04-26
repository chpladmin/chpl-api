package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.util.ValidationUtils;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadOptionalStandardReviewer")
@Log4j2
public class OptionalStandardReviewer implements Reviewer {
    private Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap = null;
    private CertificationResultRules certResultRules;
    private ValidationUtils validationUtils;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public OptionalStandardReviewer(OptionalStandardDAO optionalStandardDao,
            CertificationResultRules certResultRules,
            ValidationUtils validationUtils,
            ErrorMessageUtil msgUtil) {
        try {
            optionalStandardCriteriaMap = optionalStandardDao.getAllOptionalStandardCriteriaMap().stream()
                    .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not load Optional Standard Criteria maps", ex);
            return;
        }

        this.certResultRules = certResultRules;
        this.validationUtils = validationUtils;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(certResult -> validationUtils.isEligibleForErrors(certResult))
                .forEach(certResult -> reviewCertificationResult(listing, certResult));
        listing.getCertificationResults().stream()
                .forEach(certResult -> removeOptionalStandardsIfNotApplicable(certResult));
    }

    private void reviewCertificationResult(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveOptionalStandards(listing, certResult);
        if (!CollectionUtils.isEmpty(certResult.getOptionalStandards())) {
            certResult.getOptionalStandards().stream()
                    .forEach(optionalStandard -> reviewOptionalStandardFields(listing, certResult, optionalStandard));
        }
    }

    private void reviewCriteriaCanHaveOptionalStandards(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.OPTIONAL_STANDARD)) {
            if (!CollectionUtils.isEmpty(certResult.getOptionalStandards())) {
                listing.addWarningMessage(msgUtil.getMessage(
                        "listing.criteria.optionalStandardsNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
                certResult.setOptionalStandards(null);
            }
        }
    }

    private void removeOptionalStandardsIfNotApplicable(CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getId(), CertificationResultRules.OPTIONAL_STANDARD)) {
            certResult.setOptionalStandards(null);
        }
    }

    private void reviewOptionalStandardFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultOptionalStandard optionalStandard) {
        reviewIdRequired(listing, certResult, optionalStandard);
        reviewValueRequired(listing, certResult, optionalStandard);
        reviewFuzzyMatchHappened(listing, certResult, optionalStandard);
        reviewOptionalStandardIsValidForCriterion(listing, certResult, optionalStandard);
    }

    private void reviewIdRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultOptionalStandard optionalStandard) {
        if ((optionalStandard.getOptionalStandard() == null
                || optionalStandard.getOptionalStandard().getId() == null)
                && !StringUtils.isEmpty(optionalStandard.getUserEnteredValue())) {
            String optStdDisplay = (optionalStandard.getOptionalStandard() != null
                    && optionalStandard.getOptionalStandard().getDisplayValue() != null) ? optionalStandard.getOptionalStandard().getDisplayValue()
                            : optionalStandard.getUserEnteredValue();
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.optionalStandardNotFound",
                            Util.formatCriteriaNumber(certResult.getCriterion()),
                            optStdDisplay));
        }
    }

    private void reviewValueRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultOptionalStandard optionalStandard) {
        if (StringUtils.isEmpty(optionalStandard.getUserEnteredValue())
                && StringUtils.isEmpty(optionalStandard.getOptionalStandard().getDisplayValue())) {
            listing.addDataErrorMessage(
                    msgUtil.getMessage("listing.criteria.missingOptionalStandardName",
                            Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewFuzzyMatchHappened(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultOptionalStandard optionalStandard) {
        if (optionalStandard.getOptionalStandard() != null
                && optionalStandard.getOptionalStandard().getId() != null
                && !StringUtils.isEmpty(optionalStandard.getUserEnteredValue())
                && !userEnteredValueMatchesDisplayValue(optionalStandard.getUserEnteredValue(), optionalStandard.getOptionalStandard())
                && !userEnteredValueMatchesCitation(optionalStandard.getUserEnteredValue(), optionalStandard.getOptionalStandard())) {
            listing.addWarningMessage(
                    msgUtil.getMessage("listing.criteria.optionalStandardFuzzyMatch",
                            optionalStandard.getUserEnteredValue(),
                            optionalStandard.getOptionalStandard().getDisplayValue(),
                            Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean userEnteredValueMatchesDisplayValue(String userEnteredValue, OptionalStandard optionalStandard) {
        return !StringUtils.isEmpty(userEnteredValue)
                && optionalStandard != null
                && !StringUtils.isEmpty(optionalStandard.getDisplayValue())
                && StringUtils.equals(userEnteredValue, optionalStandard.getDisplayValue());
    }

    private boolean userEnteredValueMatchesCitation(String userEnteredValue, OptionalStandard optionalStandard) {
        return !StringUtils.isEmpty(userEnteredValue)
                && optionalStandard != null
                && !StringUtils.isEmpty(optionalStandard.getCitation())
                && StringUtils.equals(userEnteredValue, optionalStandard.getCitation());
    }

    private void reviewOptionalStandardIsValidForCriterion(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultOptionalStandard optionalStandard) {
        if (optionalStandard.getOptionalStandard() != null
                && optionalStandard.getOptionalStandard().getId() != null
                && !isOptionalStandardValidForCriteria(optionalStandard.getOptionalStandard().getId(),
                        certResult.getCriterion().getId())) {
            listing.addDataErrorMessage(msgUtil.getMessage("listing.criteria.optionalStandard.invalidCriteria",
                    optionalStandard.getOptionalStandard().getDisplayValue(),
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private boolean isOptionalStandardValidForCriteria(Long osId, Long criteriaId) {
        if (optionalStandardCriteriaMap.containsKey(criteriaId)) {
            return optionalStandardCriteriaMap.get(criteriaId).stream()
                    .filter(oscm -> oscm.getOptionalStandard().getId().equals(osId))
                    .findAny()
                    .isPresent();
        } else {
            return false;
        }
    }
}
