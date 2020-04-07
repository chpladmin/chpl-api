package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component
public class NewSurveillanceRemovedCriteriaReviewer implements Reviewer {

    private CertificationCriterionDAO certDao;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private FF4j ff4j;

    @Autowired
    public NewSurveillanceRemovedCriteriaReviewer(
            CertificationCriterionDAO certDao,
            ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions,
            FF4j ff4j) {
        this.certDao = certDao;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.ff4j = ff4j;
    }

    @Override
    public void review(Surveillance surv) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        } else if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
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
                                req.getRequirementName()));
            }
        }
    }

    private void checkNonconformityForRemovedCriteria(Surveillance surv, SurveillanceNonconformity nc) {
        if (!StringUtils.isEmpty(nc.getNonconformityType())) {
            List<CertificationCriterionDTO> criteria = certDao.getAllByNumber(nc.getNonconformityType());
            if (criteria != null && criteria.size() > 0) {
                CertificationCriterionDTO criterion = criteria.get(0);
                if (criterion != null && criterion.getRemoved() != null
                        && criterion.getRemoved().booleanValue()) {
                    surv.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedCriteria",
                                    Util.formatCriteriaNumber(criterion)));
                }
            }
        }
    }

    private void checkNonconformityForRemovedTransparency(Surveillance surv, SurveillanceNonconformity nc) {
        String requirement = nc.getNonconformityType();
        if (requirement != null && requirement.equalsIgnoreCase(NonconformityType.K2.getName())) {
            surv.getErrorMessages().add(
                    msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedRequirement",
                            nc.getNonconformityType()));
        }
    }
}
