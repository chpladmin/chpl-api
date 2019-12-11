package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class SurveillanceNonconformityReviewer implements Reviewer {
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
                            .add(msgUtil.getMessage("surveillance.nonConformityNotFound", req.getRequirement()));
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
                            msgUtil.getMessage("surveillance.requirementNonConformityMismatch", req.getRequirement()));
                }
            }
        }
    }

    private void checkNonconformityTypeValidity(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc, List<CertificationResultDetailsDTO> certResults) {
        if (StringUtils.isEmpty(nc.getNonconformityType())) {
            surv.getErrorMessages().add(
                    msgUtil.getMessage("surveillance.nonConformityTypeRequired", req.getRequirement()));
        } else {
            nc.setNonconformityType(gov.healthit.chpl.util.Util
                    .coerceToCriterionNumberFormat(nc.getNonconformityType()));
            Optional<CertificationResultDetailsDTO> attestedCertResult =
                    certResults.stream()
                    .filter(certResult -> isCriteriaAttestedTo(certResult, nc.getNonconformityType()))
                    .findFirst();
            if (!attestedCertResult.isPresent()) {
                //nonconformity type is not a criterion the listing attested to, but it could be one of a few other values
                if (!NonconformityType.K1.getName().equals(nc.getNonconformityType())
                        && !NonconformityType.K2.getName().equals(nc.getNonconformityType())
                        && !NonconformityType.L.getName().equals(nc.getNonconformityType())
                        && !NonconformityType.OTHER.getName().equals(nc.getNonconformityType())) {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.nonConformityTypeMatchException",
                                    nc.getNonconformityType(), NonconformityType.K1.getName(),
                                    NonconformityType.K2.getName(), NonconformityType.L.getName(),
                                    NonconformityType.OTHER.getName()));
                }
            }
        }
    }

    private boolean isCriteriaAttestedTo(CertificationResultDetailsDTO certResult, String criterionNumber) {
        return !StringUtils.isEmpty(certResult.getNumber())
                && certResult.getSuccess() != null
                && certResult.getSuccess().booleanValue()
                && certResult.getNumber().equals(criterionNumber);
    }

    private void checkNonconformityStatusValidity(Surveillance surv,
            SurveillanceRequirement req, SurveillanceNonconformity nc) {
        if (nc.getStatus() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.nonConformityStatusNotFound",
                    req.getRequirement(), nc.getNonconformityType()));
        } else if (nc.getStatus().getId() == null || nc.getStatus().getId().longValue() <= 0) {
            SurveillanceNonconformityStatus ncStatus = survDao
                    .findSurveillanceNonconformityStatusType(nc.getStatus().getName());
            if (ncStatus == null) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.nonConformityStatusWithNameNotFound",
                                nc.getStatus().getName(), req.getRequirement(),
                                nc.getNonconformityType()));
            } else {
                nc.setStatus(ncStatus);
            }
        } else {
            SurveillanceNonconformityStatus ncStatus = survDao
                    .findSurveillanceNonconformityStatusType(nc.getStatus().getId());
            if (ncStatus == null) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.nonConformityStatusWithIdNotFound",
                                nc.getStatus().getId(), req.getRequirement(),
                                nc.getNonconformityType()));
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
                    req.getRequirement(), nc.getNonconformityType()));
        }

        if (!StringUtils.isEmpty(nc.getCapEndDate()) && StringUtils.isEmpty(nc.getCapStartDate())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPStartIsRequired",
                    req.getRequirement(), nc.getNonconformityType()));
        }

        if (!StringUtils.isEmpty(nc.getCapEndDate()) && StringUtils.isEmpty(nc.getCapApprovalDate())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateCAPApprovalIsRequired",
                    req.getRequirement(), nc.getNonconformityType()));
        }

        if (!StringUtils.isEmpty(nc.getCapEndDate()) && !StringUtils.isEmpty(nc.getCapStartDate())
                && nc.getCapEndDate().compareTo(nc.getCapStartDate()) < 0) {
            surv.getErrorMessages()
                    .add(msgUtil.getMessage("surveillance.dateCAPEndNotGreaterThanDateCAPStart",
                            req.getRequirement(), nc.getNonconformityType()));
        }
    }

    private void checkDateOfDeterminationExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (nc.getDateOfDetermination() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateOfDeterminationIsRequired",
                    req.getRequirement(), nc.getNonconformityType()));
        }
    }

    private void checkSummaryExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (StringUtils.isEmpty(nc.getSummary())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.summaryIsRequired",
                    req.getRequirement(), nc.getNonconformityType()));
        }
    }

    private void checkFindingsExists(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (StringUtils.isEmpty(nc.getFindings())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.findingsAreRequired",
                    req.getRequirement(), nc.getNonconformityType()));
        }
    }

    private void checkSiteCountsValidityForRandomizedSurveillance(Surveillance surv, SurveillanceRequirement req,
            SurveillanceNonconformity nc) {
        if (surv.getType() != null && surv.getType().getName() != null
                && surv.getType().getName().equalsIgnoreCase("Randomized")) {
            if (nc.getSitesPassed() == null || nc.getSitesPassed().intValue() < 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.numberOfSitesPassedIsRequired",
                                req.getRequirement(), nc.getNonconformityType()));
            }

            if (nc.getTotalSites() == null || nc.getTotalSites().intValue() < 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.totalNumberOfSitesIsRequired",
                                req.getRequirement(), nc.getNonconformityType()));
            }

            if (nc.getSitesPassed() > nc.getTotalSites()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.tooManySitesPassed",
                        req.getRequirement(), nc.getNonconformityType()));
            }

            if (nc.getTotalSites() > surv.getRandomizedSitesUsed()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.tooManyTotalSites",
                        req.getRequirement(), nc.getNonconformityType()));
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
                                req.getRequirement(), nc.getNonconformityType()));
            }

            if (nc.getTotalSites() != null && nc.getTotalSites().intValue() >= 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.totalNumberOfSitesNotApplicable",
                                req.getRequirement(), nc.getNonconformityType()));
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
                                req.getRequirement(), nc.getNonconformityType()));
            }
        } else if (nc.getStatus() != null && nc.getStatus().getName() != null
                && nc.getStatus().getName().equalsIgnoreCase("Open")) {
            if (!StringUtils.isEmpty(nc.getResolution())) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resolutionDescriptionNotApplicable",
                                req.getRequirement(), nc.getNonconformityType()));
            }
        }
    }
}
