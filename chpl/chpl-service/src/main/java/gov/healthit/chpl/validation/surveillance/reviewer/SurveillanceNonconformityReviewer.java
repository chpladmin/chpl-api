package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
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
    private SurveillanceDAO survDao;
    private ErrorMessageUtil msgUtil;
    private CertificationCriterionService criterionService;

    @Autowired
    public SurveillanceNonconformityReviewer(SurveillanceDAO survDao, CertificationResultDetailsDAO certResultDetailsDao,
            ErrorMessageUtil msgUtil, CertificationCriterionService criterionService) {
        this.survDao = survDao;
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
                            .add(msgUtil.getMessage("surveillance.nonConformityNotFound", req.getRequirementName()));
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
                                    req.getRequirementName()));
                }
            }
        }
    }

    private void checkNonconformityTypeValidity(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc, List<CertificationResultDetailsDTO> certResults) {
        if (StringUtils.isEmpty(nc.getNonconformityType()) && nc.getCriterion() == null) {
            surv.getErrorMessages().add(
                    msgUtil.getMessage("surveillance.nonConformityTypeRequired",
                            req.getRequirementName()));
        } else if (nc.getCriterion() != null) {
            nc.setNonconformityType(nc.getCriterion().getNumber());
            Optional<CertificationResultDetailsDTO> attestedCertResult = certResults.stream()
                    .filter(certResult -> isCriteriaAttestedTo(certResult, nc.getCriterion()))
                    .findFirst();
            if (!attestedCertResult.isPresent()) {
                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonConformityTypeMatchException",
                                nc.getNonconformityTypeName(),
                                NonconformityType.K1.getName(), NonconformityType.K2.getName(),
                                NonconformityType.L.getName(), NonconformityType.OTHER.getName()));
            }
        } else {
            nc.setNonconformityType(criterionService.coerceToCriterionNumberFormat(nc.getNonconformityType()));
            // nonconformity type is not a criterion but it could be one of a few other values
            if (!NonconformityType.K1.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.K2.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.L.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.OTHER.getName().equals(nc.getNonconformityType())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.nonConformityTypeMatchException",
                                nc.getNonconformityTypeName(), NonconformityType.K1.getName(),
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
        if (!StringUtils.isEmpty(nc.getCapApprovalDate())
                && StringUtils.isEmpty(nc.getCapMustCompleteDate())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPMustCompleteIsRequired",
                    req.getRequirementName(),
                    nc.getNonconformityTypeName()));
        }

        if (!StringUtils.isEmpty(nc.getCapEndDate()) && StringUtils.isEmpty(nc.getCapStartDate())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPStartIsRequired",
                    req.getRequirementName(),
                    nc.getNonconformityTypeName()));
        }

        if (!StringUtils.isEmpty(nc.getCapEndDate()) && StringUtils.isEmpty(nc.getCapApprovalDate())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPApprovalIsRequired",
                    req.getRequirementName(),
                    nc.getNonconformityTypeName()));
        }

        if (!StringUtils.isEmpty(nc.getCapEndDate()) && !StringUtils.isEmpty(nc.getCapStartDate())
                && nc.getCapEndDate().compareTo(nc.getCapStartDate()) < 0) {
            surv.getErrorMessages()
                    .add(msgUtil.getMessage("surveillance.dateCAPEndNotGreaterThanDateCAPStart",
                            req.getRequirementName(),
                            nc.getNonconformityTypeName()));
        }
    }

    private void checkDateOfDeterminationExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (nc.getDateOfDetermination() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateOfDeterminationIsRequired",
                    req.getRequirementName(),
                    nc.getNonconformityTypeName()));
        }
    }

    private void checkSummaryExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (StringUtils.isEmpty(nc.getSummary())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.summaryIsRequired",
                    req.getRequirementName(),
                    nc.getNonconformityTypeName()));
        }
    }

    private void checkFindingsExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (StringUtils.isEmpty(nc.getFindings())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.findingsAreRequired",
                    req.getRequirementName(),
                    nc.getNonconformityTypeName()));
        }
    }

    private void checkSiteCountsValidityForRandomizedSurveillance(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (surv.getType() != null && surv.getType().getName() != null
                && surv.getType().getName().equalsIgnoreCase("Randomized")) {
            if (nc.getSitesPassed() == null || nc.getSitesPassed().intValue() < 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.numberOfSitesPassedIsRequired",
                                req.getRequirementName(),
                                nc.getNonconformityTypeName()));
            }

            if (nc.getTotalSites() == null || nc.getTotalSites().intValue() < 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.totalNumberOfSitesIsRequired",
                                req.getRequirementName(),
                                nc.getNonconformityTypeName()));
            }

            if (nc.getSitesPassed() != null && nc.getTotalSites() != null
                    && nc.getSitesPassed() > nc.getTotalSites()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.tooManySitesPassed",
                        req.getRequirementName(),
                        nc.getNonconformityTypeName()));
            }

            if (nc.getTotalSites() != null && nc.getTotalSites() > surv.getRandomizedSitesUsed()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.tooManyTotalSites",
                        req.getRequirementName(),
                        nc.getNonconformityTypeName()));
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
                                req.getRequirementName(),
                                nc.getNonconformityTypeName()));
            }

            if (nc.getTotalSites() != null && nc.getTotalSites().intValue() >= 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.totalNumberOfSitesNotApplicable",
                                req.getRequirementName(),
                                nc.getNonconformityTypeName()));
            }
        }
    }

    private void checkResolution(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (nc.getNonconformityCloseDate() != null) {
            if (StringUtils.isEmpty(nc.getResolution())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resolutionDescriptionIsRequired",
                                req.getRequirementName(),
                                nc.getNonconformityTypeName()));
            }
        } else if (nc.getNonconformityCloseDate() == null) {
            if (!StringUtils.isEmpty(nc.getResolution())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resolutionDescriptionNotApplicable",
                                req.getRequirementName(),
                                nc.getNonconformityTypeName()));
            }
        }
    }
}
