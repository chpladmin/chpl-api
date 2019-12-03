package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.Set;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class RemovedCriteriaComparisonReviewer implements ComparisonReviewer {

    private CertificationCriterionDAO criterionDao;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private FF4j ff4j;

    @Autowired
    public RemovedCriteriaComparisonReviewer(CertificationCriterionDAO criterionDao, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions, FF4j ff4j) {
        this.criterionDao = criterionDao;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.ff4j = ff4j;
    }

    public void review(Surveillance existingSurveillance, Surveillance updatedSurveillance) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        } else if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            for (SurveillanceRequirement req : updatedSurveillance.getRequirements()) {
                SurveillanceRequirement existingReq = findExistingRequirement(req, existingSurveillance.getRequirements());
                //if it's a new requirement it can't have any removed criteria
                if (existingReq == null && hasRemovedCriteria(req)) {
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotAddedForRemovedCriteria",
                                        req.getRequirement()));
                } else if (existingReq != null && hasRemovedCriteria(req)
                        && !req.matches(existingReq)) {
                    //if it's an existing requirement with a removed criteria then it can't
                    //be edited
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotEditedForRemovedCriteria",
                                        req.getRequirement()));
                }
            }
        }
    }

    private SurveillanceRequirement findExistingRequirement(
            SurveillanceRequirement updatedReq, Set<SurveillanceRequirement> existingReqs) {
        SurveillanceRequirement existing = null;
        for (SurveillanceRequirement existingReq : existingReqs) {
            if (updatedReq.getId() != null && existingReq.getId() != null
                    && updatedReq.getId().equals(existingReq.getId())) {
                existing = existingReq;
            }
        }
        return existing;
    }

    private boolean hasRemovedCriteria(SurveillanceRequirement req) {
        if (req.getType() != null && !StringUtils.isEmpty(req.getType().getName())) {
            if (req.getType().getName().equalsIgnoreCase(SurveillanceReviewerUtils.CRITERION_REQUIREMENT_TYPE)) {
                req.setRequirement(
                        gov.healthit.chpl.util.Util.coerceToCriterionNumberFormat(req.getRequirement()));
                CertificationCriterionDTO criterion = criterionDao.getByName(req.getRequirement());
                if (criterion != null && criterion.getRemoved() != null
                        && criterion.getRemoved().booleanValue()) {
                    return true;
                }
            }
        }
        return false;
    }
}
