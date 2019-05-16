package gov.healthit.chpl.validation.surveillance;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.dao.CertificationResultDetailsDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.Surveillance;
import gov.healthit.chpl.domain.SurveillanceNonconformity;
import gov.healthit.chpl.domain.SurveillanceNonconformityStatus;
import gov.healthit.chpl.domain.SurveillanceRequirement;
import gov.healthit.chpl.domain.SurveillanceRequirementType;
import gov.healthit.chpl.domain.SurveillanceResultType;
import gov.healthit.chpl.domain.SurveillanceType;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.surveillance.SurveillanceEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.ValidationUtils;

/**
 * Validate surveillance.
 */
@Component("surveillanceValidator")
public class SurveillanceValidator {
    private static final Logger LOGGER = LogManager.getLogger(SurveillanceValidator.class);

    private static final String CRITERION_REQUIREMENT_TYPE = "Certified Capability";
    private static final String TRANSPARENCY_REQUIREMENT_TYPE = "Transparency or Disclosure Requirement";
    private static final String HAS_NON_CONFORMITY = "Non-Conformity";

    @Autowired
    private SurveillanceDAO survDao;
    @Autowired
    private CertifiedProductDAO cpDao;
    @Autowired
    private CertificationResultDetailsDAO certResultDetailsDao;;
    @Autowired
    private CertificationCriterionDAO criterionDao;
    @Autowired
    private ErrorMessageUtil msgUtil;

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
        CertifiedProductDetailsDTO cpDetails = null;

        // make sure chpl id is valid
        if (surv.getCertifiedProduct() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.nullCertifiedProduct"));
        } else if (surv.getCertifiedProduct().getId() == null
                && surv.getCertifiedProduct().getChplProductNumber() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.nullCertifiedProductAndChplNumber"));
        } else if (surv.getCertifiedProduct().getId() == null || surv.getCertifiedProduct().getId().longValue() <= 0) {
            // the id is null, try to lookup by unique chpl number
            String chplId = surv.getCertifiedProduct().getChplProductNumber();
            if (chplId.startsWith("CHP-")) {
                try {
                    CertifiedProductDTO chplProduct = cpDao.getByChplNumber(chplId);
                    if (chplProduct != null) {
                        cpDetails = cpDao.getDetailsById(chplProduct.getId());
                        if (cpDetails != null) {
                            surv.setCertifiedProduct(new CertifiedProduct(cpDetails));
                        } else {
                            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.certifiedProductIdNotFound",
                                    chplId, chplProduct.getId()));
                        }
                    } else {
                        surv.getErrorMessages().add(msgUtil.getMessage("surveillance.productIdNotFound", chplId));
                    }
                } catch (final EntityRetrievalException ex) {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.productDetailsRetrievalException", chplId));
                }
            } else {
                try {
                    cpDetails = cpDao.getByChplUniqueId(chplId);
                    if (cpDetails != null) {
                        surv.setCertifiedProduct(new CertifiedProduct(cpDetails));
                    } else {
                        surv.getErrorMessages().add(msgUtil.getMessage("surveillance.productUniqueIdNotFound", chplId));
                    }
                } catch (final EntityRetrievalException ex) {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.productDetailsRetrievalException", chplId));
                }
            }
        } else if (surv.getCertifiedProduct().getId() != null) {
            try {
                cpDetails = cpDao.getDetailsById(surv.getCertifiedProduct().getId());
                surv.setCertifiedProduct(new CertifiedProduct(cpDetails));
            } catch (final EntityRetrievalException ex) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.detailsNotFoundForCertifiedProduct",
                        surv.getCertifiedProduct().getId()));
            }
        }

        if (!StringUtils.isEmpty(surv.getSurveillanceIdToReplace()) && surv.getCertifiedProduct() != null) {
            SurveillanceEntity existing = survDao.getSurveillanceByCertifiedProductAndFriendlyId(
                    surv.getCertifiedProduct().getId(), surv.getSurveillanceIdToReplace());
            if (existing == null) {
                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.surveillanceIdNotFound", surv.getSurveillanceIdToReplace()));
            }
        }

        if (surv.getStartDate() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.startDateRequired"));
        }

        if (surv.getType() == null) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.typeRequired"));
        } else if (surv.getType().getId() == null || surv.getType().getId().longValue() <= 0) {
            SurveillanceType survType = survDao.findSurveillanceType(surv.getType().getName());
            if (survType == null) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.typeMismatch", surv.getType().getName()));
            } else {
                surv.setType(survType);
            }
        } else {
            SurveillanceType survType = survDao.findSurveillanceType(surv.getType().getId());
            if (survType == null) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.typeNotFound", surv.getType().getId()));
            } else {
                surv.setType(survType);
            }
        }

        // randomized surveillance requires number of sites used but
        // any other type of surveillance should not have that value
        if (surv.getType() != null && surv.getType().getName() != null
                && surv.getType().getName().equalsIgnoreCase("Randomized")) {
            if (surv.getRandomizedSitesUsed() == null || surv.getRandomizedSitesUsed().intValue() < 0) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.randomizedNonzeroValue"));
            }
        } else if (surv.getType() != null && surv.getType().getName() != null
                && !surv.getType().getName().equalsIgnoreCase("Randomized")) {
            if (surv.getRandomizedSitesUsed() != null && surv.getRandomizedSitesUsed().intValue() >= 0) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.randomizedSitesNotApplicable", surv.getType().getName()));
            }
        }

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

        if (surv.getType() != null) {
            addSurveillanceWarningIfNotValid(surv, surv.getType().getName(), "Surveillance Type");
        }

        if (checkAuthority) {
            validateSurveillanceAuthority(surv);
        }
        validateSurveillanceRequirements(surv, certResults);
        validateSurveillanceNonconformities(surv, certResults);
    }

    /**
     * Validate the requirements in a surveillance.
     *
     * @param surv
     *            the surveillance
     * @param certResults
     *            certification results of the relevant certified product
     */
    public void validateSurveillanceRequirements(final Surveillance surv,
            final List<CertificationResultDetailsDTO> certResults) {
        if (surv.getRequirements() == null || surv.getRequirements().size() == 0) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.requirementIsRequiredForProduct",
                    surv.getCertifiedProduct().getChplProductNumber()));
        } else {
            for (SurveillanceRequirement req : surv.getRequirements()) {
                if (StringUtils.isEmpty(req.getRequirement())) {
                    surv.getErrorMessages().add(msgUtil.getMessage("surveillance.requirementIsRequired"));
                }

                if (req.getType() == null) {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.typeMissingForRequirement", req.getRequirement()));
                } else if (req.getType().getId() == null || req.getType().getId().longValue() <= 0) {
                    SurveillanceRequirementType reqType = survDao
                            .findSurveillanceRequirementType(req.getType().getName());
                    if (reqType == null) {
                        surv.getErrorMessages().add(msgUtil.getMessage("surveillance.typeNameMissingForRequirement",
                                req.getType().getName(), req.getRequirement()));
                    } else {
                        req.setType(reqType);
                    }
                } else {
                    SurveillanceRequirementType reqType = survDao
                            .findSurveillanceRequirementType(req.getType().getId());
                    if (reqType == null) {
                        surv.getErrorMessages().add(msgUtil.getMessage("surveillance.typeIdMissingForRequirement",
                                req.getType().getId(), req.getRequirement()));
                    } else {
                        req.setType(reqType);
                    }
                }

                // the surveillance requirement validation is different
                // depending on the requirement type
                if (req.getType() != null && !StringUtils.isEmpty(req.getType().getName())) {
                    if (req.getType().getName().equalsIgnoreCase(CRITERION_REQUIREMENT_TYPE)
                            && surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {

                        req.setRequirement(
                                gov.healthit.chpl.util.Util.coerceToCriterionNumberFormat(req.getRequirement()));
                        CertificationCriterionDTO criterion = null;
                        // see if the nonconformity type is a criterion that the
                        // product has attested to
                        // List<CertificationResultDetailsDTO> certResults =
                        // certResultDetailsDao.getCertificationResultDetailsByCertifiedProductId(
                        // surv.getCertifiedProduct().getId());
                        if (certResults != null && certResults.size() > 0) {
                            for (CertificationResultDetailsDTO certResult : certResults) {
                                if (!StringUtils.isEmpty(certResult.getNumber()) && certResult.getSuccess() != null
                                        && certResult.getSuccess()
                                        && certResult.getNumber().equals(req.getRequirement())) {
                                    criterion = criterionDao.getByName(req.getRequirement());
                                }
                            }
                        }
                        if (criterion == null) {
                            surv.getErrorMessages()
                                    .add(msgUtil.getMessage("surveillance.requirementInvalidForRequirementType",
                                            req.getRequirement(), req.getType().getName()));
                        }
                    } else if (req.getType().getName().equals(TRANSPARENCY_REQUIREMENT_TYPE)) {
                        // requirement has to be one of 170.523 (k)(1) or (k)(2)
                        req.setRequirement(
                                gov.healthit.chpl.util.Util.coerceToCriterionNumberFormat(req.getRequirement()));
                        if (!RequirementTypeEnum.K1.getName().equals(req.getRequirement())
                                && !RequirementTypeEnum.K2.getName().equals(req.getRequirement())) {
                            surv.getErrorMessages()
                                    .add(msgUtil.getMessage("surveillance.requirementInvalidForTransparencyType",
                                            req.getRequirement(), req.getType().getName(),
                                            RequirementTypeEnum.K1.getName(), RequirementTypeEnum.K2.getName()));
                        }
                    }
                } else {
                    surv.getErrorMessages()
                            .add(msgUtil.getMessage("surveillance.requirementMustHaveValue", req.getRequirement()));
                }

                if (surv.getEndDate() != null) {
                    if (req.getResult() == null) {
                        surv.getErrorMessages()
                                .add(msgUtil.getMessage("surveillance.resultNotFound", req.getRequirement()));
                    }
                }

                if (req.getResult() != null
                        && (req.getResult().getId() == null || req.getResult().getId().longValue() <= 0)) {
                    SurveillanceResultType resType = survDao.findSurveillanceResultType(req.getResult().getName());
                    if (resType == null) {
                        surv.getErrorMessages().add(msgUtil.getMessage("surveillance.resultWithNameNotFound",
                                req.getResult().getName(), req.getRequirement()));
                    } else {
                        req.setResult(resType);
                    }
                } else if (req.getResult() != null) {
                    SurveillanceResultType resType = survDao.findSurveillanceResultType(req.getResult().getId());
                    if (resType == null) {
                        surv.getErrorMessages().add(msgUtil.getMessage("surveillance.resultWithIdNotFound",
                                req.getResult().getId(), req.getRequirement()));
                    } else {
                        req.setResult(resType);
                    }
                }

                addSurveillanceWarningIfNotValid(surv, req.getRequirement(),
                        "Requirement '" + req.getRequirement() + "'");
            }
        }
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

    private void addSurveillanceWarningIfNotValid(final Surveillance surv, final String input, final String fieldName) {
        if (!ValidationUtils.isValidUtf8(input)) {
            surv.getWarningMessages().add(msgUtil.getMessage("surveillance.badCharacterFound", fieldName));
        }
        if (ValidationUtils.hasNewline(input)) {
            surv.getWarningMessages().add(msgUtil.getMessage("surveillance.newlineCharacterFound", fieldName));
        }
    }
}
