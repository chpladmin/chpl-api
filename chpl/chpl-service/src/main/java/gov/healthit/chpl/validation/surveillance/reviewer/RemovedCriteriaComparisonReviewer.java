package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;
import java.util.Optional;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
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
            for (SurveillanceRequirement updatedReq : updatedSurveillance.getRequirements()) {
                //look for an existing surv requirement that matches the updated requirement
                //and check for removed criteria and/or updates to the requirement
                Optional<SurveillanceRequirement> existingReq
                    = existingSurveillance.getRequirements().stream()
                        .filter(existingSurvReq ->
                            doRequirementsMatch(updatedReq, existingSurvReq)
                            && hasRemovedCriteria(updatedReq))
                        .findFirst();

                if (!existingReq.isPresent()) {
                    //if it's a new requirement it can't have any removed criteria
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotAddedForRemovedCriteria",
                                    updatedReq.getRequirement()));
                } else if (updatedReq.matches(existingReq.get())){
                    //if it's an existing requirement with a removed criteria then it can't
                    //be edited
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotEditedForRemovedCriteria",
                                    updatedReq.getRequirement()));
                }

                checkNonconformitiesForRemovedCriteria(updatedReq.getNonconformities());
            }
        }
    }

    private void checkNonconformitiesForRemovedCriteria(List<SurveillanceNonconformity> nonconformities) {

    }

    private boolean doRequirementsMatch(SurveillanceRequirement updatedReq, SurveillanceRequirement existingReq) {
        return updatedReq.getId() != null && existingReq.getId() != null
                && updatedReq.getId().equals(existingReq.getId());
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
