package gov.healthit.chpl.validation.surveillance;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.validation.surveillance.reviewer.ChplProductNumberReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.RandomizedSurveillanceReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.RequiredFieldsReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.Reviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceIdReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.SurveillanceRequirementReviewer;
import gov.healthit.chpl.validation.surveillance.reviewer.UnsupportedCharacterReviewer;

/**
 * Validate surveillance.
 */
@Component("surveillanceValidator")
public class SurveillanceValidator {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceValidator.class);

    private SurveillanceDAO survDao;
    private CertifiedProductDAO cpDao;
    private CertificationResultDetailsDAO certResultDetailsDao;;
    private CertificationCriterionDAO criterionDao;
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    private List<Reviewer> reviewers;

    @Autowired
    public SurveillanceValidator(ChplProductNumberReviewer chplNumberReviewer,
            SurveillanceIdReviewer survIdReviewer, RequiredFieldsReviewer requiredFieldsReviewer,
            RandomizedSurveillanceReviewer randomizedReviewer, UnsupportedCharacterReviewer characterReviewer,
            SurveillanceRequirementReviewer survReqReviewer) {
        reviewers = new ArrayList<Reviewer>();
        reviewers.add(chplNumberReviewer);
        reviewers.add(survIdReviewer);
        reviewers.add(requiredFieldsReviewer);
        reviewers.add(randomizedReviewer);
        reviewers.add(characterReviewer);
        reviewers.add(survReqReviewer);
    }

    /**
     * Validate a surveillance.
     *
     * @param surv
     *            the surveillance to validate
     * @param checkAuthority
     *            indicates if the authority should be checked during the
     *            validation
     */
    public void validate(final Surveillance surv, final Boolean checkAuthority) {
        for (Reviewer reviewer : reviewers) {
            reviewer.review(surv);
        }


//TODO move the rest into reviewer classes
        //add comparison reviewers for OCD-3098
        List<CertificationResultDetailsDTO> certResults = null;
        if (surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
            try {
                certResults = certResultDetailsDao
                        .getCertificationResultDetailsByCertifiedProductId(surv.getCertifiedProduct().getId());
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Could not find cert results for certified product " + surv.getCertifiedProduct().getId(),
                        ex);
            }
        }

        if (checkAuthority) {
            validateSurveillanceAuthority(surv);
        }
        validateSurveillanceNonconformities(surv, certResults);
    }

    /**
     * Validate nonconformities related to the surveillance.
     *
     * @param surv
     *            the surveillance
     * @param certResults
     *            certification results of the relevant certified product
     */
    public void validateSurveillanceNonconformities(final Surveillance surv,
            final List<CertificationResultDetailsDTO> certResults) {
        if (surv.getRequirements() == null) {
            return;
        }
        // assume surveillance requires a close date until proven otherwise
        boolean requiresCloseDate = true;
        for (SurveillanceRequirement req : surv.getRequirements()) {
            if (req.getResult() != null && !StringUtils.isEmpty(req.getResult().getName())
                    && req.getResult().getName().equalsIgnoreCase(HAS_NON_CONFORMITY)) {
                // there should be nonconformities
                if (req.getNonconformities() == null || req.getNonconformities().size() == 0) {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.nonConformityNotFound", req.getRequirement()));
                } else {
                    for (SurveillanceNonconformity nc : req.getNonconformities()) {
                        if (StringUtils.isEmpty(nc.getNonconformityType())) {
                            surv.getErrorMessages().add(
                                    msgUtil.getMessage("surveillance.nonConformityTypeRequired", req.getRequirement()));
                        } else {
                            // non-conformity type is not empty. is a
                            // certification criteria or just a string?
                            CertificationCriterionDTO criterion = null;
                            if (surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
                                nc.setNonconformityType(gov.healthit.chpl.util.Util
                                        .coerceToCriterionNumberFormat(nc.getNonconformityType()));
                                // see if the nonconformity type is a criterion
                                // that the product has attested to
                                if (certResults != null && certResults.size() > 0) {
                                    for (CertificationResultDetailsDTO certResult : certResults) {
                                        if (!StringUtils.isEmpty(certResult.getNumber())
                                                && certResult.getSuccess() != null && certResult.getSuccess()
                                                && certResult.getNumber().equals(nc.getNonconformityType())) {
                                            criterion = criterionDao.getByName(nc.getNonconformityType());
                                            if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE) && criterion != null
                                                    && criterion.getRemoved() != null && criterion.getRemoved().booleanValue()) {
                                                surv.getErrorMessages().add(
                                                        msgUtil.getMessage("surveillance.requirementInvalidForRemovedCriteria",
                                                                req.getRequirement(), criterion.getNumber()));
                                            }
                                        }
                                    }
                                }
                            }
                            // if it could have matched a criterion but didn't,
                            // it has to be one of a few other values
                            if (surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null
                                    && criterion == null) {
                                nc.setNonconformityType(gov.healthit.chpl.util.Util
                                        .coerceToCriterionNumberFormat(nc.getNonconformityType()));
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

                        if (nc.getDateOfDetermination() == null) {
                            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.dateOfDeterminationIsRequired",
                                    req.getRequirement(), nc.getNonconformityType()));
                        }

                        if (StringUtils.isEmpty(nc.getSummary())) {
                            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.summaryIsRequired",
                                    req.getRequirement(), nc.getNonconformityType()));
                        }

                        if (StringUtils.isEmpty(nc.getFindings())) {
                            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.findingsAreRequired",
                                    req.getRequirement(), nc.getNonconformityType()));
                        }

                        // site counts are required for completed randomized
                        // surveillance
                        // but not allowed for other types of surveillance
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
                        } else if (surv.getType() != null && surv.getType().getName() != null
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
                            requiresCloseDate = false;
                        }

                        addSurveillanceWarningIfNotValid(surv, nc.getDeveloperExplanation(),
                                "Developer Explanation '" + nc.getDeveloperExplanation() + "'");
                        addSurveillanceWarningIfNotValid(surv, nc.getFindings(), "Findings '" + nc.getFindings() + "'");
                        addSurveillanceWarningIfNotValid(surv, nc.getNonconformityType(),
                                "Nonconformity Type '" + nc.getNonconformityType() + "'");
                        addSurveillanceWarningIfNotValid(surv, nc.getResolution(),
                                "Resolution '" + nc.getResolution() + "'");
                        addSurveillanceWarningIfNotValid(surv, nc.getSummary(), "Summary '" + nc.getSummary() + "'");
                    }
                }
            } else {
                if (req.getNonconformities() != null && req.getNonconformities().size() > 0) {
                    surv.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNonConformityMismatch", req.getRequirement()));
                }
            }
        }
        if (requiresCloseDate && surv.getEndDate() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.endDateRequiredNoOpenNonConformities"));
        }
    }

    /**
     * Validate that user has correct authority on surveillance. Valid authorities are ROLE_ONC or ROLE_ACB
     *
     * @param surv the surveillance to check
     */
    public void validateSurveillanceAuthority(final Surveillance surv) {
        if (!StringUtils.isEmpty(surv.getAuthority())) {
            if (!surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ONC)
                    && !surv.getAuthority().equalsIgnoreCase(Authority.ROLE_ACB)) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.authorityRequired",
                        Authority.ROLE_ONC, Authority.ROLE_ACB));
            }
        }
    }

}
