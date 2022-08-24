package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
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
            checkRequirementForRemovedCriteria(surv, req);
            checkRequirementForRemovedTransparency(surv, req);
            for (SurveillanceNonconformity nc : req.getNonconformities()) {
                checkNonconformityForRemovedCriteria(surv, nc);
                checkNonconformityForRemovedTransparency(surv, nc);
            }
        }
    }

    private void checkRequirementForRemovedCriteria(Surveillance surv, SurveillanceRequirement req) {
        if (req.getType() != null && !StringUtils.isEmpty(req.getType().getName())
                && req.getType().getName().equalsIgnoreCase(SurveillanceRequirementType.CERTIFIED_CAPABILITY)) {
                CertificationCriterionDTO criterion = certDao.getAllByNumber(req.getRequirement()).get(0);
                if (criterion != null && criterion.getRemoved() != null
                        && criterion.getRemoved().booleanValue()) {
                    surv.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotAddedForRemovedCriteria",
                                    Util.formatCriteriaNumber(criterion)));
                }
        }
    }

    private void checkRequirementForRemovedTransparency(Surveillance surv, SurveillanceRequirement req) {
        if (req.getType() != null && !StringUtils.isEmpty(req.getType().getName())
                && req.getType().getName().equalsIgnoreCase(SurveillanceRequirementType.TRANS_DISCLOSURE_REQ)) {
            String requirement = req.getRequirement();
            if (requirement != null && requirement.equalsIgnoreCase(RequirementTypeEnum.K2.getName())) {
                surv.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.requirementNotAddedForRemovedRequirement",
                                req.getRequirement()));
            }
        }
    }

    private void checkNonconformityForRemovedCriteria(Surveillance surv, SurveillanceNonconformity nc) {
        if (NullSafeEvaluator.eval(() -> nc.getType().getRemoved(), false)) {
            surv.getErrorMessages().add(
                    msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedCriteria",
                            CertificationCriterionService.formatCriteriaNumber(nc.getType().getNumber(), nc.getType().getTitle())));
        }

        //if (!StringUtils.isEmpty(nc.getNonconformityType())) {
        //    List<CertificationCriterionDTO> criteria = certDao.getAllByNumber(nc.getNonconformityType());
        //    if (criteria != null && criteria.size() > 0) {
        //        CertificationCriterionDTO criterion = criteria.get(0);
        //        if (criterion != null && criterion.getRemoved() != null
        //                && criterion.getRemoved().booleanValue()) {
        //            surv.getErrorMessages().add(
        //                    msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedCriteria",
        //                            Util.formatCriteriaNumber(criterion)));
        //        }
        //    }
        //}
    }

    private void checkNonconformityForRemovedTransparency(Surveillance surv, SurveillanceNonconformity nc) {
        //TODO - TMY - can we do this differently now? (OCD-4029
        if (Objects.equals(
                NullSafeEvaluator.eval(() -> nc.getType().getTitle(), null),
                "170.523 (k)(2)")) {
            surv.getErrorMessages().add(
                    msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedRequirement",
                    nc.getType().getNumber()));
        }

        //String requirement = nc.getNonconformityType();
        //if (requirement != null && requirement.equalsIgnoreCase(NonconformityType.K2.getName())) {
        //    surv.getErrorMessages().add(
        //            msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedRequirement",
        //                    nc.getNonconformityType()));
        //}
    }
}
