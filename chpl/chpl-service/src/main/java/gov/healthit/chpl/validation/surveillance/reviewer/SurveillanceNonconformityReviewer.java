package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SurveillanceNonconformityReviewer implements Reviewer {
    private static final Long NOT_FOUND = -1L;

    private CertificationResultDetailsDAO certResultDetailsDao;
    private ErrorMessageUtil msgUtil;
    private CertificationCriterionService criterionService;
    private DimensionalDataManager dimensionalDataManager;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;

    @Autowired
    public SurveillanceNonconformityReviewer(CertificationResultDetailsDAO certResultDetailsDao,
            ErrorMessageUtil msgUtil, CertificationCriterionService criterionService, DimensionalDataManager dimensionalDataManager,
            @Lazy CertifiedProductDetailsManager certifiedProductDetailsManager) {
        this.certResultDetailsDao = certResultDetailsDao;
        this.msgUtil = msgUtil;
        this.criterionService = criterionService;
        this.dimensionalDataManager = dimensionalDataManager;
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
    }

    @Override
    public void review(Surveillance surv) {
        if (surv.getRequirements() == null) {
            return;
        }
        List<CertificationResultDetailsDTO> certResults = certResultDetailsDao
                .getCertificationResultsForSurveillanceListing(surv);

        // assume surveillance requires a close date until proven otherwise
        for (SurveillanceRequirement req : surv.getRequirements()) {
            if (req.getResult() != null && !StringUtils.isEmpty(req.getResult().getName())
                    && req.getResult().getName().equalsIgnoreCase(SurveillanceResultType.NON_CONFORMITY)) {
                // there should be nonconformities
                if (req.getNonconformities() == null || req.getNonconformities().size() == 0) {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.nonConformityNotFound", req.getRequirementType().getFormattedTitle()));
                } else {
                    for (SurveillanceNonconformity nc : req.getNonconformities()) {
                        checkNonconformityTypeValidity(surv, req, nc);
                        checkCorrectiveActionPlanDatesValidity(surv, req, nc);
                        checkDateOfDeterminationExists(surv, req, nc);
                        checkSummaryExists(surv, req, nc);
                        checkFindingsExists(surv, req, nc);
                        checkSiteCountsValidityForRandomizedSurveillance(surv, req, nc);
                        checkSiteCountsValidityForNonRandomizedSurveillance(surv, req, nc);
                        checkResolution(surv, req, nc);
                        checkCriteriaEditionMatchesListingEdition(surv, req, nc);
                    }
                }
            } else {
                if (req.getNonconformities() != null && req.getNonconformities().size() > 0) {
                    surv.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNonConformityMismatch", req.getRequirementType().getFormattedTitle()));
                }
            }
        }
    }

    private void checkNonconformityTypeValidity(Surveillance surv, SurveillanceRequirement req, SurveillanceNonconformity nc) {
        if (NullSafeEvaluator.eval(() -> nc.getType().getId(), -1).equals(-1)) {
            surv.getErrorMessages().add(
                    msgUtil.getMessage("surveillance.nonConformityTypeRequired",
                            req.getRequirementType().getFormattedTitle()));
            return;
        }
    }

    private void checkCorrectiveActionPlanDatesValidity(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (nc.getCapApprovalDay() != null && nc.getCapMustCompleteDay() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPMustCompleteIsRequired",
                    req.getRequirementType().getFormattedTitle(),
                    nc.getType().getFormattedTitle()));
        }

        if (nc.getCapEndDay() != null && nc.getCapStartDay() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPStartIsRequired",
                    req.getRequirementType().getFormattedTitle(),
                    nc.getType().getFormattedTitle()));
        }

        if (nc.getCapEndDay() != null && nc.getCapApprovalDay() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPApprovalIsRequired",
                    req.getRequirementType().getFormattedTitle(),
                    nc.getType().getFormattedTitle()));
        }

        if (nc.getCapEndDay() != null && nc.getCapStartDay() != null
                && nc.getCapEndDay().isBefore(nc.getCapStartDay())) {
            surv.getErrorMessages()
                    .add(msgUtil.getMessage("surveillance.dateCAPEndNotGreaterThanDateCAPStart",
                            req.getRequirementType().getFormattedTitle(),
                            nc.getType().getFormattedTitle()));
        }
    }

    private void checkDateOfDeterminationExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (nc.getDateOfDeterminationDay() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateOfDeterminationIsRequired",
                    req.getRequirementType().getFormattedTitle(),
                    nc.getType().getFormattedTitle()));
        }
    }

    private void checkSummaryExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (StringUtils.isEmpty(nc.getSummary())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.summaryIsRequired",
                    req.getRequirementType().getFormattedTitle(),
                    nc.getType().getFormattedTitle()));
        }
    }

    private void checkFindingsExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (StringUtils.isEmpty(nc.getFindings())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.findingsAreRequired",
                    req.getRequirementType().getFormattedTitle(),
                    nc.getType().getFormattedTitle()));
        }
    }

    private void checkSiteCountsValidityForRandomizedSurveillance(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (surv.getType() != null && surv.getType().getName() != null
                && surv.getType().getName().equalsIgnoreCase("Randomized")) {
            if (nc.getSitesPassed() == null || nc.getSitesPassed().intValue() < 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.numberOfSitesPassedIsRequired",
                                req.getRequirementType().getFormattedTitle(),
                                nc.getType().getFormattedTitle()));
            }

            if (nc.getTotalSites() == null || nc.getTotalSites().intValue() < 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.totalNumberOfSitesIsRequired",
                                req.getRequirementType().getFormattedTitle(),
                                nc.getType().getFormattedTitle()));
            }

            if (nc.getSitesPassed() != null && nc.getTotalSites() != null
                    && nc.getSitesPassed() > nc.getTotalSites()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.tooManySitesPassed",
                        req.getRequirementType().getFormattedTitle(),
                        nc.getType().getFormattedTitle()));
            }

            if (nc.getTotalSites() != null && nc.getTotalSites() > surv.getRandomizedSitesUsed()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.tooManyTotalSites",
                        req.getRequirementType().getFormattedTitle(),
                        nc.getType().getFormattedTitle()));
            }
        }
    }

    private void checkSiteCountsValidityForNonRandomizedSurveillance(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (surv.getType() != null && surv.getType().getName() != null
                && !surv.getType().getName().equalsIgnoreCase("Randomized")) {
            if (nc.getSitesPassed() != null && nc.getSitesPassed().intValue() >= 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.numberOfSitesPassedNotApplicable",
                                req.getRequirementType().getFormattedTitle(),
                                nc.getType().getFormattedTitle()));
            }

            if (nc.getTotalSites() != null && nc.getTotalSites().intValue() >= 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.totalNumberOfSitesNotApplicable",
                                req.getRequirementType().getFormattedTitle(),
                                nc.getType().getFormattedTitle()));
            }
        }
    }

    private void checkResolution(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (nc.getNonconformityCloseDay() != null) {
            if (StringUtils.isEmpty(nc.getResolution())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resolutionDescriptionIsRequired",
                                req.getRequirementType().getFormattedTitle(),
                                nc.getType().getFormattedTitle()));
            }
        } else if (nc.getNonconformityCloseDay() == null) {
            if (!StringUtils.isEmpty(nc.getResolution())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resolutionDescriptionNotApplicable",
                                req.getRequirementType().getFormattedTitle(),
                                nc.getType().getFormattedTitle()));
            }
        }
    }

    private void checkCriteriaEditionMatchesListingEdition(Surveillance surv, SurveillanceRequirement req, SurveillanceNonconformity nc) {
        if (isNonconformityTypeCertifiedCapability(nc.getType().getId())) {
            Long nonconformityCriteriaEdition = getCertificationEditionIdFromNonconformityType(nc.getType().getId());
            if (!nonconformityCriteriaEdition.equals(NOT_FOUND)) {
                Long listingEdition = getCertificationEditionIdFromListing(surv.getCertifiedProduct().getId());
                if (!nonconformityCriteriaEdition.equals(listingEdition)) {
                    surv.getErrorMessages().add(msgUtil.getMessage("surveillance.nonconformityTypeEditionMismatch",
                            getNonConformityType(nc.getType().getId()).getFormattedTitle()));
                }
            }
        }
    }

    private Long getCertificationEditionIdFromNonconformityType(Long nonconformityTypeId) {
        return NullSafeEvaluator.eval(() -> criterionService.get(nonconformityTypeId).getCertificationEditionId(), NOT_FOUND);
    }

    private boolean isNonconformityTypeCertifiedCapability(Long nonconformtiyTypeId) {
        NonconformityType nonconformityTypFromDb = getNonConformityType(nonconformtiyTypeId);
        return nonconformityTypFromDb != null
                ? nonconformityTypFromDb.getClassification().equals(NonconformityClassification.CRITERION)
                : false;
    }

    private NonconformityType getNonConformityType(Long id) {
        return dimensionalDataManager.getNonconformityTypes().stream()
                .filter(nc -> nc.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    private Long getCertificationEditionIdFromListing(Long id) {
        try {
            return Long.valueOf(certifiedProductDetailsManager.getCertifiedProductDetails(id)
                    .getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString());
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve listing", e);
            return NOT_FOUND;
        }
    }
}
