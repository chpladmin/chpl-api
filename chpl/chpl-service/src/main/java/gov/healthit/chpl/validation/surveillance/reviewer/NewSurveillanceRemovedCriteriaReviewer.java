package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;

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
            checkRequirementForRemovedRequirementType(surv, req);
            for (SurveillanceNonconformity nc : req.getNonconformities()) {
                checkForRemovedNonconformity(surv, nc);
            }
        }
    }

    private void checkRequirementForRemovedRequirementType(Surveillance surv, SurveillanceRequirement req) {
        if (NullSafeEvaluator.eval(() -> req.getRequirementType().getRemoved(), false)) {
            surv.getErrorMessages().add(
                    msgUtil.getMessage("surveillance.requirementNotAddedForRemoved", req.getRequirementType().getFormattedTitle()));
        }
    }

    private void checkForRemovedNonconformity(Surveillance surv, SurveillanceNonconformity nc) {
        if (NullSafeEvaluator.eval(() -> nc.getType().getRemoved(), false)) {
            surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotAddedForRemoved", nc.getType().getFormattedTitle()));
        }
    }
}
