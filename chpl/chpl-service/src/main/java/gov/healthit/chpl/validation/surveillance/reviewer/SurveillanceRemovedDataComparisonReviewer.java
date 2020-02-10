package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;
import java.util.Optional;

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

@Component("surveillanceRemovedDataComparisonReviewer")
public class SurveillanceRemovedDataComparisonReviewer implements ComparisonReviewer {

    private CertificationCriterionDAO criterionDao;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private FF4j ff4j;

    @Autowired
    public SurveillanceRemovedDataComparisonReviewer(CertificationCriterionDAO criterionDao, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions, FF4j ff4j) {
        this.criterionDao = criterionDao;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.ff4j = ff4j;
    }

    /**
     * Removed criteria cannot be added to requirements or nonconformities by ACBs.
     * If a removed criteria is already referenced by a requirement or nonconformity,
     * then the details of that item cannot be edited by an ACB.
     */
    @Override
    public void review(Surveillance existingSurveillance, Surveillance updatedSurveillance) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        } else if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            return;
        }

        for (SurveillanceRequirement updatedReq : updatedSurveillance.getRequirements()) {
            //look for an existing surv requirement that matches the updated requirement
            //and check for removed criteria and/or updates to the requirement
            Optional<SurveillanceRequirement> existingReq
                = existingSurveillance.getRequirements().stream()
                    .filter(existingSurvReq ->
                        doRequirementsMatch(updatedReq, existingSurvReq))
                    .findFirst();

            if (!existingReq.isPresent() && hasRemovedCriteria(updatedReq)) {
                //if it's a new requirement it can't have any removed criteria
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.requirementNotAddedForRemovedCriteria",
                                updatedReq.getRequirement()));
                checkNonconformitiesForRemovedCriteria(updatedSurveillance, null, updatedReq.getNonconformities());
            } else if (!existingReq.isPresent() && hasRemovedRequirement(updatedReq)) {
                //if it's a new requirement it can't have transparency requirements
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.requirementNotAddedForRemovedRequirement",
                                updatedReq.getRequirement()));
                checkNonconformitiesForRemovedRequirement(updatedSurveillance, null, updatedReq.getNonconformities());
            } else if (existingReq.isPresent()) {
                if (hasRemovedCriteria(updatedReq) && !updatedReq.matches(existingReq.get())) {
                    //if it's an existing requirement with a removed criteria then it can't be edited
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotEditedForRemovedCriteria",
                                    updatedReq.getRequirement()));
                } else if (hasRemovedRequirement(updatedReq) && !updatedReq.matches(existingReq.get())) {
                    //if it's an existing requirement with transparency then it can't be edited
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotEditedForRemovedRequirement",
                                    updatedReq.getRequirement()));
                }
                checkNonconformitiesForRemovedCriteria(
                        updatedSurveillance, existingReq.get().getNonconformities(), updatedReq.getNonconformities());
                checkNonconformitiesForRemovedRequirement(
                        updatedSurveillance, existingReq.get().getNonconformities(), updatedReq.getNonconformities());
            }
        }
    }

    /**
     * Look for new or updated nonconformities that reference removed criteria.
     * Add error message if found.
     */
    private void checkNonconformitiesForRemovedCriteria(Surveillance updatedSurveillance,
            List<SurveillanceNonconformity> existingSurvNonconformities,
            List<SurveillanceNonconformity> updatedSurvNonconformities) {
        for (SurveillanceNonconformity updatedNonconformity : updatedSurvNonconformities) {
            //look for an existing nonconformity that matches the updated nonconformity
            //and check for removed criteria and/or updates to the nonconformity
            Optional<SurveillanceNonconformity> existingNonconformity
                = existingSurvNonconformities.stream()
                    .filter(existingSurvNonconformity ->
                        doNonconformitiesMatch(updatedNonconformity, existingSurvNonconformity))
                    .findFirst();

            if (!existingNonconformity.isPresent() && hasRemovedCriteria(updatedNonconformity)) {
                //if it's a new nonconformity it can't have any removed criteria
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedCriteria",
                                updatedNonconformity.getNonconformityType()));
            } else if (existingNonconformity.isPresent()
                    && hasRemovedCriteria(updatedNonconformity)
                    && !updatedNonconformity.matches(existingNonconformity.get())) {
                    //if it's an existing nonconformity with a removed criteria then it can't be edited
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.nonconformityNotEditedForRemovedCriteria",
                                    updatedNonconformity.getNonconformityType()));
            }
        }
    }

    private void checkNonconformitiesForRemovedRequirement(Surveillance updatedSurveillance,
            List<SurveillanceNonconformity> existingSurvNonconformities,
            List<SurveillanceNonconformity> updatedSurvNonconformities) {
        for (SurveillanceNonconformity updatedNonconformity : updatedSurvNonconformities) {
            //look for an existing nonconformity that matches the updated nonconformity
            //and check for removed transparency and/or updates to the nonconformity
            Optional<SurveillanceNonconformity> existingNonconformity
                = existingSurvNonconformities.stream()
                    .filter(existingSurvNonconformity ->
                        doNonconformitiesMatch(updatedNonconformity, existingSurvNonconformity))
                    .findFirst();

            if (!existingNonconformity.isPresent() && hasRemovedCriteria(updatedNonconformity)) {
                //if it's a new nonconformity it can't have any removed criteria
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedCriteria",
                                updatedNonconformity.getNonconformityType()));
            } else if (!existingNonconformity.isPresent() && hasRemovedRequirement(updatedNonconformity)) {
                //if it's a new nonconformity it can't have transparency
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedRequirement",
                                updatedNonconformity.getNonconformityType()));
            } else if (existingNonconformity.isPresent()
                    && hasRemovedCriteria(updatedNonconformity)
                    && !updatedNonconformity.matches(existingNonconformity.get())) {
                    //if it's an existing nonconformity with a removed criteria then it can't be edited
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.nonconformityNotEditedForRemovedCriteria",
                                    updatedNonconformity.getNonconformityType()));
            } else if (existingNonconformity.isPresent()
                    && hasRemovedRequirement(updatedNonconformity)
                    && !updatedNonconformity.matches(existingNonconformity.get())) {
                    //if it's an existing nonconformity with transparency then it can't be edited
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.nonconformityNotEditedForRemovedRequirement",
                                    updatedNonconformity.getNonconformityType()));
            }
        }
    }

    /**
     * Determine if two surveillance requirements have the same ID.
     */
    private boolean doRequirementsMatch(SurveillanceRequirement updatedReq, SurveillanceRequirement existingReq) {
        return updatedReq.getId() != null && existingReq.getId() != null
                && updatedReq.getId().equals(existingReq.getId());
    }

    /**
     * Determine if two nonconformities have the same ID.
     */
    private boolean doNonconformitiesMatch(SurveillanceNonconformity updatedNonconformity,
            SurveillanceNonconformity existingNonconformity) {
        return updatedNonconformity.getId() != null && existingNonconformity.getId() != null
                && updatedNonconformity.getId().equals(existingNonconformity.getId());
    }

    /**
     * Determine if a surveillance requirement references a removed criteria.
     */
    private boolean hasRemovedCriteria(SurveillanceRequirement req) {
        if (req.getType() != null && !StringUtils.isEmpty(req.getType().getName())) {
            if (req.getType().getName().equalsIgnoreCase(SurveillanceRequirementType.CERTIFIED_CAPABILITY)) {
                String requirementCriteria =
                        gov.healthit.chpl.util.Util.coerceToCriterionNumberFormat(req.getRequirement());
                CertificationCriterionDTO criterion = criterionDao.getByName(requirementCriteria);
                if (criterion != null && criterion.getRemoved() != null
                        && criterion.getRemoved().booleanValue()) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasRemovedRequirement(SurveillanceRequirement req) {
        if (req.getType() != null && !StringUtils.isEmpty(req.getType().getName())) {
            if (req.getType().getName().equalsIgnoreCase(SurveillanceRequirementType.TRANS_DISCLOSURE_REQ)) {
                String requirement = req.getRequirement();
                if (requirement != null && requirement.equalsIgnoreCase(RequirementTypeEnum.K2.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine if a nonconformity references a removed criteria.
     */
    private boolean hasRemovedCriteria(SurveillanceNonconformity nonconformity) {
        if (!StringUtils.isEmpty(nonconformity.getNonconformityType())) {
            String nonconformityType =
                    gov.healthit.chpl.util.Util.coerceToCriterionNumberFormat(nonconformity.getNonconformityType());
            CertificationCriterionDTO criterion = criterionDao.getByName(nonconformityType);
            if (criterion != null && criterion.getRemoved() != null
                    && criterion.getRemoved().booleanValue()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRemovedRequirement(SurveillanceNonconformity nonconformity) {
        if (!StringUtils.isEmpty(nonconformity.getNonconformityType())) {
            String requirement = nonconformity.getNonconformityType();
            if (requirement != null && requirement.equalsIgnoreCase(NonconformityType.K2.getName())) {
                return true;
            }
        }
        return false;
    }
}
