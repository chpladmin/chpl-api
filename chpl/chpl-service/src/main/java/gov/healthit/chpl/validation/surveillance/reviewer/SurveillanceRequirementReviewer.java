package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.compliance.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.surveillance.RequirementType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.util.DateUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class SurveillanceRequirementReviewer implements Reviewer {
    private static final Long NOT_FOUND = -1L;
    private SurveillanceDAO survDao;
    private ErrorMessageUtil msgUtil;
    private Set<RequirementType> requirementTypes;

    @Autowired
    public SurveillanceRequirementReviewer(SurveillanceDAO survDao, ErrorMessageUtil msgUtil, DimensionalDataManager dimensionalDataManager) {
        this.survDao = survDao;
        this.msgUtil = msgUtil;
        this.requirementTypes = dimensionalDataManager.getRequirementTypes();
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
            checkRequirementValidForSurveillanceStartDate(surv, req);
        });
    }

    private void checkRequirementExists(Surveillance surv, SurveillanceRequirement req) {
        if (!isRequirementTypeOther(req.getRequirementType())) {
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

    private void checkRequirementValidForSurveillanceStartDate(Surveillance surv, SurveillanceRequirement req) {
        if (isAddingNewSurveillance(surv)
            && !DateUtil.isDateBetweenInclusive(Pair.of(req.getRequirementType().getStartDay(), req.getRequirementType().getEndDay()), surv.getStartDay())) {

            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.nonConformityType.notValid", req.getRequirementType().getFormattedTitle()));
        }
    }

    private boolean isAddingNewSurveillance(Surveillance surv) {
        return surv.getId() == null;
    }

    private boolean isRequirementTypeOther(RequirementType requirementType) {
        return NullSafeEvaluator.eval(() -> requirementType.getId(), NOT_FOUND).equals(NOT_FOUND);
    }

    private RequirementType getRequirementTypeFullyPopulated(Long id) {
        return requirementTypes.stream()
                .filter(rdt -> rdt.getId().equals(id))
                .findAny()
                .orElse(null);
    }

}
