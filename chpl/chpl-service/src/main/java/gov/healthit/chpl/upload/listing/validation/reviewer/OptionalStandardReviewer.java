package gov.healthit.chpl.upload.listing.validation.reviewer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.optionalStandard.dao.OptionalStandardDAO;
import gov.healthit.chpl.optionalStandard.domain.CertificationResultOptionalStandard;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
import gov.healthit.chpl.util.CertificationResultRules;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Component("listingUploadOptionalStandardReviewer")
@Log4j2
public class OptionalStandardReviewer implements Reviewer {
    private Map<Long, List<OptionalStandardCriteriaMap>> optionalStandardCriteriaMap = null;
    private CertificationResultRules certResultRules;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public OptionalStandardReviewer(OptionalStandardDAO optionalStandardDao,
            CertificationResultRules certResultRules,
            ErrorMessageUtil msgUtil) {
        try {
            optionalStandardCriteriaMap = optionalStandardDao.getAllOptionalStandardCriteriaMap().stream()
                    .collect(Collectors.groupingBy(scm -> scm.getCriterion().getId()));
        } catch (EntityRetrievalException ex) {
            LOGGER.error("Could not load Optional Standard Criteria maps", ex);
            return;
        }

        this.certResultRules = certResultRules;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
            .filter(certResult -> BooleanUtils.isTrue(certResult.isSuccess()))
            .forEach(certResult -> reviewCertificationResult(listing, certResult));
    }

    private void reviewCertificationResult(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        reviewCriteriaCanHaveOptionalStandards(listing, certResult);
        if (!CollectionUtils.isEmpty(certResult.getOptionalStandards())) {
            certResult.getOptionalStandards().stream()
                .forEach(optionalStandard -> reviewOptionalStandardFields(listing, certResult, optionalStandard));
        }
    }

    private void reviewCriteriaCanHaveOptionalStandards(CertifiedProductSearchDetails listing, CertificationResult certResult) {
        if (!certResultRules.hasCertOption(certResult.getCriterion().getNumber(), CertificationResultRules.OPTIONAL_STANDARD)) {
            if (!CollectionUtils.isEmpty(certResult.getOptionalStandards())) {
                listing.getWarningMessages().add(msgUtil.getMessage(
                    "listing.criteria.optionalStandardsNotApplicable", Util.formatCriteriaNumber(certResult.getCriterion())));
            }
            certResult.setOptionalStandards(null);
        }
    }

    private void reviewOptionalStandardFields(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultOptionalStandard optionalStandard) {
        reviewIdRequired(listing, certResult, optionalStandard);
        reviewCitationRequired(listing, certResult, optionalStandard);
        reviewOptionalStandardIsValidForCriterion(listing, certResult, optionalStandard);
    }

    private void reviewIdRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultOptionalStandard optionalStandard) {
        if (optionalStandard.getOptionalStandardId() == null
                && !StringUtils.isEmpty(optionalStandard.getCitation())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.optionalStandardNotFound",
                    Util.formatCriteriaNumber(certResult.getCriterion()),
                    optionalStandard.getCitation()));
        }
    }

    private void reviewCitationRequired(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultOptionalStandard optionalStandard) {
        if (StringUtils.isEmpty(optionalStandard.getCitation())) {
            listing.getErrorMessages().add(
                    msgUtil.getMessage("listing.criteria.missingOptionalStandardName",
                    Util.formatCriteriaNumber(certResult.getCriterion())));
        }
    }

    private void reviewOptionalStandardIsValidForCriterion(CertifiedProductSearchDetails listing,
            CertificationResult certResult, CertificationResultOptionalStandard optionalStandard) {
        if (optionalStandard.getOptionalStandardId() != null
                && !isOptionalStandardValidForCriteria(optionalStandard.getOptionalStandardId(),
                certResult.getCriterion().getId())) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.criteria.optionalStandard.invalidCriteria",
                    optionalStandard.getCitation(), Util.formatCriteriaNumber(certResult.getCriterion())));
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
