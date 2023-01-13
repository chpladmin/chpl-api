package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.surveillance.RequirementGroupType;
import gov.healthit.chpl.domain.surveillance.RequirementType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SurveillanceRequirementReviewer implements Reviewer {
    private static final Long NOT_FOUND = -1L;
    private SurveillanceDAO survDao;
    private ErrorMessageUtil msgUtil;
    private CertifiedProductDetailsManager certifiedProductDetailsManager;
    private Set<RequirementType> requirementTypes;

    @Autowired
    public SurveillanceRequirementReviewer(SurveillanceDAO survDao, ErrorMessageUtil msgUtil, DimensionalDataManager dimensionalDataManager,
            @Lazy CertifiedProductDetailsManager certifiedProductDetailsManager) {
        this.survDao = survDao;
        this.msgUtil = msgUtil;
        this.requirementTypes = dimensionalDataManager.getRequirementTypes();
        this.certifiedProductDetailsManager = certifiedProductDetailsManager;
    }

    @Override
    public void review(Surveillance surv) {
        if (surv.getRequirements() == null || surv.getRequirements().size() == 0) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.requirementIsRequiredForProduct",
                    surv.getCertifiedProduct().getChplProductNumber()));
            return;
        }
        surv.getRequirements().forEach(req -> {
            checkRequirementExists(surv, req);
            checkResultExistsIfSurveillanceClosed(surv, req);
            checkResultTypeValidity(surv, req);
            checkCriteriaEditionMatchesListingEdition(surv, req);
        });
    }

    private void checkRequirementExists(Surveillance surv, SurveillanceRequirement req) {
        if (!NullSafeEvaluator.eval(() -> req.getRequirementType().getId(), -1L).equals(-1L)) {
            RequirementType reqDetailTypeFound = getRequirementTypeFullyPopulated(req.getRequirementType().getId());
            if (reqDetailTypeFound == null) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.requirementIsRequired"));
            }
        }
    }

    private void checkResultExistsIfSurveillanceClosed(Surveillance surv, SurveillanceRequirement req) {
        if (surv.getEndDay() != null) {
            if (req.getResult() == null) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resultNotFound", req.getRequirementType().getFormattedTitle()));
            }
        }
    }

    private void checkResultTypeValidity(Surveillance surv, SurveillanceRequirement req) {
        if (req.getResult() != null
                && (req.getResult().getId() == null || req.getResult().getId().longValue() <= 0)) {
            SurveillanceResultType resType = survDao.findSurveillanceResultType(req.getResult().getName());
            if (resType == null) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.resultWithNameNotFound",
                        req.getResult().getName(), req.getRequirementType().getFormattedTitle()));
            } else {
                req.setResult(resType);
            }
        } else if (req.getResult() != null) {
            SurveillanceResultType resType = survDao.findSurveillanceResultType(req.getResult().getId());
            if (resType == null) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.resultWithIdNotFound",
                        req.getResult().getId(), req.getRequirementType().getFormattedTitle()));
            } else {
                req.setResult(resType);
            }
        }
    }

    private void checkCriteriaEditionMatchesListingEdition(Surveillance surv, SurveillanceRequirement req) {
            if (isRequirementTypeCertifiedCapability(req.getRequirementType())) {
                Long requirementCriteriaEdition = NullSafeEvaluator.eval(() ->
                        getRequirementTypeFullyPopulated(req.getRequirementType().getId())
                                .getCertificationEdition().getCertificationEditionId(), NOT_FOUND);

                if (!requirementCriteriaEdition.equals(NOT_FOUND)) {
                    Long listingEdition = NullSafeEvaluator.eval(() -> getCertificationEditionIdFromListing(surv.getCertifiedProduct().getId()), NOT_FOUND);
                    if (!requirementCriteriaEdition.equals(listingEdition)) {
                        surv.getErrorMessages().add(msgUtil.getMessage("surveillance.requirementTypeEditionMismatch",
                                getRequirementTypeFullyPopulated(req.getRequirementType().getId()).getFormattedTitle()));
                    }
                }
            }
    }

    private boolean isRequirementTypeCertifiedCapability(RequirementType requirementType) {
        RequirementType requirementTypFromDb = getRequirementTypeFullyPopulated(requirementType.getId());
        return requirementTypFromDb != null
                ? requirementTypFromDb.getRequirementGroupType().getId().equals(RequirementGroupType.CERTIFIED_CAPABILITY_ID)
                : false;
    }

    private RequirementType getRequirementTypeFullyPopulated(Long id) {
        return requirementTypes.stream()
                .filter(rdt -> rdt.getId().equals(id))
                .findAny()
                .orElse(null);
    }

    private Long getCertificationEditionIdFromListing(Long id) {
        try {
            return Long.valueOf(certifiedProductDetailsManager.getCertifiedProductDetails(id)
                    .getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString());
        } catch (EntityRetrievalException e) {
            LOGGER.error("Could not retrieve listing", e);
            return null;
        }
    }
}
