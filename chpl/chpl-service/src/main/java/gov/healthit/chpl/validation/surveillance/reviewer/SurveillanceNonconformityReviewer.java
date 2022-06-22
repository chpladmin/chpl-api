package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class SurveillanceNonconformityReviewer implements Reviewer {
    private CertificationResultDetailsDAO certResultDetailsDao;
    private ErrorMessageUtil msgUtil;
    private CertificationCriterionService criterionService;

    @Autowired
    public SurveillanceNonconformityReviewer(CertificationResultDetailsDAO certResultDetailsDao,
            ErrorMessageUtil msgUtil, CertificationCriterionService criterionService) {
        this.certResultDetailsDao = certResultDetailsDao;
        this.msgUtil = msgUtil;
        this.criterionService = criterionService;
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
                            .add(msgUtil.getMessage("surveillance.nonConformityNotFound", req.getRequirement()));
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
                            msgUtil.getMessage("surveillance.requirementNonConformityMismatch",
                                    req.getRequirement()));
                }
            }
        }
    }

    private void checkNonconformityTypeValidity(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc, List<CertificationResultDetailsDTO> certResults) {
        if (StringUtils.isEmpty(nc.getNonconformityType()) && nc.getCriterion() == null) {
            surv.getErrorMessages().add(
                    msgUtil.getMessage("surveillance.nonConformityTypeRequired",
                            req.getRequirement()));
        } else if (nc.getCriterion() != null) {
            nc.setNonconformityType(nc.getCriterion().getNumber());
            Optional<CertificationResultDetailsDTO> attestedCertResult = certResults.stream()
                    .filter(certResult -> isCriteriaAttestedTo(certResult, nc.getCriterion()))
                    .findFirst();
            if (!attestedCertResult.isPresent()) {
                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonConformityTypeMatchException",
                                nc.getNonconformityType(),
                                NonconformityType.K1.getName(), NonconformityType.K2.getName(),
                                NonconformityType.L.getName(), NonconformityType.OTHER.getName()));
            }
        } else {
            nc.setNonconformityType(criterionService.coerceToCriterionNumberFormat(nc.getNonconformityType()));
            // nonconformity type is not a criterion but it could be one of a few other values
            if (!NonconformityType.K1.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.K2.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.L.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.OTHER.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.ANNUAL_RWT_PLAN.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.ANNUAL_RWT_RESULTS.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.SEMIANNUAL_ATTESTATIONS_SUBMISSION.getName().equals(nc.getNonconformityType())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.nonConformityTypeMatchException",
                                nc.getNonconformityType(), NonconformityType.K1.getName(),
                                NonconformityType.K2.getName(), NonconformityType.L.getName(),
                                NonconformityType.OTHER.getName()));
            }
        }
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
                    req.getRequirement(),
                    nc.getNonconformityType()));
        }

        if (nc.getCapEndDay() != null && nc.getCapStartDay() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPStartIsRequired",
                    req.getRequirement(),
                    nc.getNonconformityType()));
        }

        if (nc.getCapEndDay() != null && nc.getCapApprovalDay() != null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPApprovalIsRequired",
                    req.getRequirement(),
                    nc.getNonconformityType()));
        }

        if (nc.getCapEndDay() != null && nc.getCapStartDay() != null
                && nc.getCapEndDay().compareTo(nc.getCapStartDay()) < 0) {
            surv.getErrorMessages()
                    .add(msgUtil.getMessage("surveillance.dateCAPEndNotGreaterThanDateCAPStart",
                            req.getRequirement(),
                            nc.getNonconformityType()));
        }
    }

    private void checkDateOfDeterminationExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (nc.getDateOfDeterminationDay() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateOfDeterminationIsRequired",
                    req.getRequirement(),
                    nc.getNonconformityType()));
        }
    }

    private void checkSummaryExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (StringUtils.isEmpty(nc.getSummary())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.summaryIsRequired",
                    req.getRequirement(),
                    nc.getNonconformityType()));
        }
    }

    private void checkFindingsExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (StringUtils.isEmpty(nc.getFindings())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.findingsAreRequired",
                    req.getRequirement(),
                    nc.getNonconformityType()));
        }
    }

    private void checkSiteCountsValidityForRandomizedSurveillance(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (surv.getType() != null && surv.getType().getName() != null
                && surv.getType().getName().equalsIgnoreCase("Randomized")) {
            if (nc.getSitesPassed() == null || nc.getSitesPassed().intValue() < 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.numberOfSitesPassedIsRequired",
                                req.getRequirement(),
                                nc.getNonconformityType()));
            }

            if (nc.getTotalSites() == null || nc.getTotalSites().intValue() < 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.totalNumberOfSitesIsRequired",
                                req.getRequirement(),
                                nc.getNonconformityType()));
            }

            if (nc.getSitesPassed() != null && nc.getTotalSites() != null
                    && nc.getSitesPassed() > nc.getTotalSites()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.tooManySitesPassed",
                        req.getRequirement(),
                        nc.getNonconformityType()));
            }

            if (nc.getTotalSites() != null && nc.getTotalSites() > surv.getRandomizedSitesUsed()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.tooManyTotalSites",
                        req.getRequirement(),
                        nc.getNonconformityType()));
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
                                req.getRequirement(),
                                nc.getNonconformityType()));
            }

            if (nc.getTotalSites() != null && nc.getTotalSites().intValue() >= 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.totalNumberOfSitesNotApplicable",
                                req.getRequirement(),
                                nc.getNonconformityType()));
            }
        }
    }

    private void checkResolution(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (nc.getNonconformityCloseDay() != null) {
            if (StringUtils.isEmpty(nc.getResolution())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resolutionDescriptionIsRequired",
                                req.getRequirement(),
                                nc.getNonconformityType()));
            }
        } else if (nc.getNonconformityCloseDay() == null) {
            if (!StringUtils.isEmpty(nc.getResolution())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resolutionDescriptionNotApplicable",
                                req.getRequirement(),
                                nc.getNonconformityType()));
            }
        }
    }
}
