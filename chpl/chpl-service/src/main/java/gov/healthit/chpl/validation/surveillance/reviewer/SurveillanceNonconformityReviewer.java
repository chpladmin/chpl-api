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
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class SurveillanceNonconformityReviewer extends Reviewer {
    private CertificationResultDetailsDAO certResultDetailsDao;
    private SurveillanceDAO survDao;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public SurveillanceNonconformityReviewer(SurveillanceDAO survDao,
            CertificationResultDetailsDAO certResultDetailsDao,
            ErrorMessageUtil msgUtil) {
        this.survDao = survDao;
        this.certResultDetailsDao = certResultDetailsDao;
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(Surveillance surv) {
        if (surv.getRequirements() == null) {
            return;
        }
        List<CertificationResultDetailsDTO> certResults =
                certResultDetailsDao.getCertificationResultsForSurveillanceListing(surv);

        // assume surveillance requires a close date until proven otherwise
        for (SurveillanceRequirement req : surv.getRequirements()) {
            if (req.getResult() != null && !StringUtils.isEmpty(req.getResult().getName())
                    && req.getResult().getName().equalsIgnoreCase(SurveillanceResultType.NON_CONFORMITY)) {
                // there should be nonconformities
                if (req.getNonconformities() == null || req.getNonconformities().size() == 0) {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.nonConformityNotFound", getRequirementName(req)));
                } else {
                    for (SurveillanceNonconformity nc : req.getNonconformities()) {
                        checkNonconformityTypeValidity(surv, req, nc, certResults);
                        checkNonconformityStatusValidity(surv, req, nc);
                        checkCorrectiveActionPlanDatesValidity(surv, req, nc);
                        checkDateOfDeterminationExists(surv, req, nc);
                        checkSummaryExists(surv, req, nc);
                        checkFindingsExists(surv, req, nc);
                        checkSiteCountsValidityForRandomizedSurveillance(surv, req, nc);
                        checkSiteCountsValidityForNonRandomizedSurveillance(surv, req, nc);
                        checkStatusRequiredFields(surv, req, nc);
                    }
                }
            } else {
                if (req.getNonconformities() != null && req.getNonconformities().size() > 0) {
                    surv.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNonConformityMismatch",
                                    getRequirementName(req)));
                }
            }
        }
    }

    private void checkNonconformityTypeValidity(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc, List<CertificationResultDetailsDTO> certResults) {
        if (StringUtils.isEmpty(nc.getNonconformityType()) && nc.getCriterion() == null) {
            surv.getErrorMessages().add(
                    msgUtil.getMessage("surveillance.nonConformityTypeRequired",
                            getRequirementName(req)));
        } else if (nc.getCriterion() != null) {
            nc.setNonconformityType(nc.getCriterion().getNumber());
            Optional<CertificationResultDetailsDTO> attestedCertResult =
                    certResults.stream()
                    .filter(certResult -> isCriteriaAttestedTo(certResult, nc.getCriterion()))
                    .findFirst();
            if (!attestedCertResult.isPresent()) {
                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonConformityTypeMatchException",
                                getNonconformityTypeName(nc),
                                NonconformityType.K1.getName(), NonconformityType.K2.getName(),
                                NonconformityType.L.getName(), NonconformityType.OTHER.getName()));
            }
        } else {
            nc.setNonconformityType(gov.healthit.chpl.util.Util
                    .coerceToCriterionNumberFormat(nc.getNonconformityType()));
            //nonconformity type is not a criterion but it could be one of a few other values
            if (!NonconformityType.K1.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.K2.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.L.getName().equals(nc.getNonconformityType())
                    && !NonconformityType.OTHER.getName().equals(nc.getNonconformityType())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.nonConformityTypeMatchException",
                                getNonconformityTypeName(nc), NonconformityType.K1.getName(),
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

    private void checkNonconformityStatusValidity(Surveillance surv,
            SurveillanceRequirement req, SurveillanceNonconformity nc) {
        if (nc.getStatus() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.nonConformityStatusNotFound",
                    getRequirementName(req),
                    getNonconformityTypeName(nc)));
        } else if (nc.getStatus().getId() == null || nc.getStatus().getId().longValue() <= 0) {
            SurveillanceNonconformityStatus ncStatus = survDao
                    .findSurveillanceNonconformityStatusType(nc.getStatus().getName());
            if (ncStatus == null) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.nonConformityStatusWithNameNotFound",
                                nc.getStatus().getName(),
                                getRequirementName(req),
                                getNonconformityTypeName(nc)));
            } else {
                nc.setStatus(ncStatus);
            }
        } else {
            SurveillanceNonconformityStatus ncStatus = survDao
                    .findSurveillanceNonconformityStatusType(nc.getStatus().getId());
            if (ncStatus == null) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.nonConformityStatusWithIdNotFound",
                                nc.getStatus().getId(),
                                getRequirementName(req),
                                getNonconformityTypeName(nc)));
            } else {
                nc.setStatus(ncStatus);
            }
        }
    }

    private void checkCorrectiveActionPlanDatesValidity(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (!StringUtils.isEmpty(nc.getCapApprovalDate())
                && StringUtils.isEmpty(nc.getCapMustCompleteDate())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPMustCompleteIsRequired",
                    getRequirementName(req),
                    getNonconformityTypeName(nc)));
        }

        if (!StringUtils.isEmpty(nc.getCapEndDate()) && StringUtils.isEmpty(nc.getCapStartDate())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPStartIsRequired",
                    getRequirementName(req),
                    getNonconformityTypeName(nc)));
        }

        if (!StringUtils.isEmpty(nc.getCapEndDate()) && StringUtils.isEmpty(nc.getCapApprovalDate())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPApprovalIsRequired",
                    getRequirementName(req),
                    getNonconformityTypeName(nc)));
        }

        if (!StringUtils.isEmpty(nc.getCapEndDate()) && !StringUtils.isEmpty(nc.getCapStartDate())
                && nc.getCapEndDate().compareTo(nc.getCapStartDate()) < 0) {
            surv.getErrorMessages()
                    .add(msgUtil.getMessage("surveillance.dateCAPEndNotGreaterThanDateCAPStart",
                            getRequirementName(req),
                            getNonconformityTypeName(nc)));
        }
    }

    private void checkDateOfDeterminationExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (nc.getDateOfDetermination() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateOfDeterminationIsRequired",
                    getRequirementName(req),
                    getNonconformityTypeName(nc)));
        }
    }

    private void checkSummaryExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (StringUtils.isEmpty(nc.getSummary())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.summaryIsRequired",
                    getRequirementName(req),
                    getNonconformityTypeName(nc)));
        }
    }

    private void checkFindingsExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (StringUtils.isEmpty(nc.getFindings())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.findingsAreRequired",
                    getRequirementName(req),
                    getNonconformityTypeName(nc)));
        }
    }

    private void checkSiteCountsValidityForRandomizedSurveillance(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (surv.getType() != null && surv.getType().getName() != null
                && surv.getType().getName().equalsIgnoreCase("Randomized")) {
            if (nc.getSitesPassed() == null || nc.getSitesPassed().intValue() < 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.numberOfSitesPassedIsRequired",
                                getRequirementName(req),
                                getNonconformityTypeName(nc)));
            }

            if (nc.getTotalSites() == null || nc.getTotalSites().intValue() < 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.totalNumberOfSitesIsRequired",
                                getRequirementName(req),
                                getNonconformityTypeName(nc)));
            }

            if (nc.getSitesPassed() != null && nc.getTotalSites() != null
                    && nc.getSitesPassed() > nc.getTotalSites()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.tooManySitesPassed",
                        getRequirementName(req),
                        getNonconformityTypeName(nc)));
            }

            if (nc.getTotalSites() > surv.getRandomizedSitesUsed()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.tooManyTotalSites",
                        getRequirementName(req),
                        getNonconformityTypeName(nc)));
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
                                getRequirementName(req),
                                getNonconformityTypeName(nc)));
            }

            if (nc.getTotalSites() != null && nc.getTotalSites().intValue() >= 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.totalNumberOfSitesNotApplicable",
                                getRequirementName(req),
                                getNonconformityTypeName(nc)));
            }
        }
    }

    private void checkStatusRequiredFields(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (nc.getStatus() != null && nc.getStatus().getName() != null
                && nc.getStatus().getName().equalsIgnoreCase("Closed")) {
            if (StringUtils.isEmpty(nc.getResolution())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resolutionDescriptionIsRequired",
                                getRequirementName(req),
                                getNonconformityTypeName(nc)));
            }
        } else if (nc.getStatus() != null && nc.getStatus().getName() != null
                && nc.getStatus().getName().equalsIgnoreCase("Open")) {
            if (!StringUtils.isEmpty(nc.getResolution())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resolutionDescriptionNotApplicable",
                                getRequirementName(req),
                                getNonconformityTypeName(nc)));
            }
        }
    }
}
