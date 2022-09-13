package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;
import gov.healthit.chpl.util.Util;

@Component
public class NewSurveillanceRemovedCriteriaReviewer implements Reviewer {

    private CertificationCriterionDAO certDao;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;

    @Autowired
    public NewSurveillanceRemovedCriteriaReviewer(CertificationCriterionDAO certDao, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions) {
        this.certDao = certDao;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public void review(Surveillance surv) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        for (SurveillanceRequirement req : surv.getRequirements()) {
            checkRequirementForRemovedDetailType(surv, req);
            for (SurveillanceNonconformity nc : req.getNonconformities()) {
                checkForRemovedNonconformity(surv, nc);
            }
        }
    }

    private void checkRequirementForRemovedDetailType(Surveillance surv, SurveillanceRequirement req) {
        if (NullSafeEvaluator.eval(() -> req.getRequirementDetailType().getRemoved(), false)) {
            if (NullSafeEvaluator.eval(() -> req.getRequirementDetailType().getSurveillanceRequirementType().getId(), -1)
                    .equals(SurveillanceRequirementType.CERTIFIED_CAPABILITY_ID)) {

                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.requirementNotAddedForRemovedCriteria", Util.formatCriteriaNumber(req.getRequirementDetailType())));
            } else if (NullSafeEvaluator.eval(() -> req.getRequirementDetailType().getSurveillanceRequirementType().getId(), -1)
                    .equals(SurveillanceRequirementType.TRANS_DISCLOSURE_ID)) {

                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.requirementNotAddedForRemovedRequirement", req.getRequirementDetailType().getTitle()));

            }
        }
    }

    private void checkForRemovedNonconformity(Surveillance surv, SurveillanceNonconformity nc) {
        if (NullSafeEvaluator.eval(() -> nc.getType().getRemoved(), false)) {
            if (NullSafeEvaluator.eval(() -> nc.getType().getClassification(), NonconformityClassification.UNKNOWN).equals(NonconformityClassification.CRITERION)) {
                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedCriteria",
                                Util.formatCriteriaNumber(nc.getType())));
            } else if (NullSafeEvaluator.eval(() -> nc.getType().getClassification(), NonconformityClassification.UNKNOWN).equals(NonconformityClassification.REQUIREMENT)) {
                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedRequirement",
                        nc.getType().getNumber()));
            }
        }
    }
}
