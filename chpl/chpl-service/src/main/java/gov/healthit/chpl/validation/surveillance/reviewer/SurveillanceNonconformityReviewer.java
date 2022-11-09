package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;

@Component
public class SurveillanceNonconformityReviewer implements Reviewer {
    private CertificationResultDetailsDAO certResultDetailsDao;
    private ErrorMessageUtil msgUtil;
    private CertificationCriterionService criterionService;
    private DimensionalDataManager dimensionalDataManager;

    @Autowired
    public SurveillanceNonconformityReviewer(CertificationResultDetailsDAO certResultDetailsDao,
            ErrorMessageUtil msgUtil, CertificationCriterionService criterionService, DimensionalDataManager dimensionalDataManager) {
        this.certResultDetailsDao = certResultDetailsDao;
        this.msgUtil = msgUtil;
        this.criterionService = criterionService;
        this.dimensionalDataManager = dimensionalDataManager;
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
                        checkNonconformityTypeValidity(surv, req, nc, certResults);
                        checkCorrectiveActionPlanDatesValidity(surv, req, nc);
                        checkDateOfDeterminationExists(surv, req, nc);
                        checkSummaryExists(surv, req, nc);
                        checkFindingsExists(surv, req, nc);
                        checkSiteCountsValidityForRandomizedSurveillance(surv, req, nc);
                        checkSiteCountsValidityForNonRandomizedSurveillance(surv, req, nc);
                        checkResolution(surv, req, nc);
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

    private void checkNonconformityTypeValidity(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc, List<CertificationResultDetailsDTO> certResults) {
        if (NullSafeEvaluator.eval(() -> nc.getType().getId(), -1).equals(-1)) {
            surv.getErrorMessages().add(
                    msgUtil.getMessage("surveillance.nonConformityTypeRequired",
                            req.getRequirementType().getFormattedTitle()));
            return;
        }

        NonconformityType ncFromDb = getNonconformityType(nc.getType().getId());

        if (ncFromDb.getClassification().equals(NonconformityClassification.CRITERION)) {
            CertificationCriterion criterion = criterionService.get(ncFromDb.getId());

            Optional<CertificationResultDetailsDTO> attestedCertResult = certResults.stream()
                    .filter(certResult -> isCriteriaAttestedTo(certResult, criterion))
                    .findFirst();

            if (!attestedCertResult.isPresent()) {
                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonConformityTypeMatchException",
                                nc.getType().getFormattedTitle()));
            }
        }
    }

    private NonconformityType getNonconformityType(Long nonconformityTypeId) {
        return dimensionalDataManager.getNonconformityTypes().stream()
                .filter(nc -> nc.getId().equals(nonconformityTypeId))
                .findFirst()
                .orElse(null);
    }

    private boolean isCriteriaAttestedTo(CertificationResultDetailsDTO certResult, CertificationCriterion criterion) {
        return certResult.getCriterion() != null
                && certResult.getSuccess() != null
                && certResult.getSuccess().booleanValue()
                && certResult.getCriterion().getId().equals(criterion.getId());
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
}
