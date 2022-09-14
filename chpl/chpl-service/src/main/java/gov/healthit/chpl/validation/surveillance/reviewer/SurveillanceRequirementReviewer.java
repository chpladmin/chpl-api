package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.surveillance.SurveillanceDAO;
import gov.healthit.chpl.domain.surveillance.RequirementDetailType;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceResultType;
import gov.healthit.chpl.manager.DimensionalDataManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;

@Component
public class SurveillanceRequirementReviewer implements Reviewer {
    private SurveillanceDAO survDao;
    private ErrorMessageUtil msgUtil;
    private Set<RequirementDetailType> requirementDetailTypes;

    @Autowired
    public SurveillanceRequirementReviewer(SurveillanceDAO survDao, ErrorMessageUtil msgUtil, DimensionalDataManager dimensionalDataManager) {
        this.survDao = survDao;
        this.msgUtil = msgUtil;
        this.requirementDetailTypes = dimensionalDataManager.getRequirementDetailTypes();
    }

    @Override
    public void review(Surveillance surv) {
        if (surv.getRequirements() == null || surv.getRequirements().size() == 0) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.requirementIsRequiredForProduct",
                    surv.getCertifiedProduct().getChplProductNumber()));
            return;
        }
        for (SurveillanceRequirement req : surv.getRequirements()) {
            checkRequirementExists(surv, req);
            checkResultExistsIfSurveillanceClosed(surv, req);
            checkResultTypeValidity(surv, req);
        }
    }

    private void checkRequirementExists(Surveillance surv, SurveillanceRequirement req) {
        if (NullSafeEvaluator.eval(() -> req.getRequirementDetailType().getId(), -1L).equals(-1L)) {
            Optional<RequirementDetailType> reqDetailTypeFound = requirementDetailTypes.stream()
                    .filter(rdt -> rdt.getId().equals(req.getRequirementDetailType().getId()))
                    .findAny();
            if (reqDetailTypeFound.isEmpty()) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.requirementIsRequired"));
            }
        }
    }

    private void checkResultExistsIfSurveillanceClosed(Surveillance surv, SurveillanceRequirement req) {
        if (surv.getEndDay() != null) {
            if (req.getResult() == null) {
                surv.getErrorMessages()
                        .add(msgUtil.getMessage("surveillance.resultNotFound", req.getRequirementDetailType().getFormattedTitle()));
            }
        }
    }

    private void checkResultTypeValidity(Surveillance surv, SurveillanceRequirement req) {
        if (req.getResult() != null
                && (req.getResult().getId() == null || req.getResult().getId().longValue() <= 0)) {
            SurveillanceResultType resType = survDao.findSurveillanceResultType(req.getResult().getName());
            if (resType == null) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.resultWithNameNotFound",
                        req.getResult().getName(), req.getRequirementDetailType().getFormattedTitle()));
            } else {
                req.setResult(resType);
            }
        } else if (req.getResult() != null) {
            SurveillanceResultType resType = survDao.findSurveillanceResultType(req.getResult().getId());
            if (resType == null) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.resultWithIdNotFound",
                        req.getResult().getId(), req.getRequirementDetailType().getFormattedTitle()));
            } else {
                req.setResult(resType);
            }
        }
    }
}
