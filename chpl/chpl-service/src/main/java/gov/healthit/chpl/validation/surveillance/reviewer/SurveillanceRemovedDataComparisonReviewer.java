package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertificationCriterionDAO;
import gov.healthit.chpl.domain.NonconformityType;
import gov.healthit.chpl.domain.concept.RequirementTypeEnum;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.CertificationCriterionService;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("surveillanceRemovedDataComparisonReviewer")
public class SurveillanceRemovedDataComparisonReviewer implements ComparisonReviewer {

    private CertificationCriterionDAO criterionDao;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;
    private CertificationCriterionService criterionService;

    @Autowired
    public SurveillanceRemovedDataComparisonReviewer(CertificationCriterionDAO criterionDao, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions, CertificationCriterionService criterionService) {
        this.criterionDao = criterionDao;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
        this.criterionService = criterionService;
    }

    @Override
    public void review(Surveillance existingSurveillance, Surveillance updatedSurveillance) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        for (SurveillanceRequirement updatedReq : updatedSurveillance.getRequirements()) {
            // look for an existing surv requirement that matches the updated requirement
            // and check for removed criteria and/or updates to the requirement
            Optional<SurveillanceRequirement> existingReq = existingSurveillance.getRequirements().stream()
                    .filter(existingSurvReq -> doRequirementsMatch(updatedReq, existingSurvReq))
                    .findFirst();

            if (!existingReq.isPresent() && hasRemovedCriteria(updatedReq)) {
                // if it's a new requirement it can't have any removed criteria
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.requirementNotAddedForRemovedCriteria",
                                updatedReq.getRequirementName()));
                checkNonconformitiesForRemovedCriteria(updatedSurveillance, null, updatedReq.getNonconformities());
            } else if (!existingReq.isPresent() && hasRemovedRequirement(updatedReq)) {
                // if it's a new requirement it can't have transparency requirements
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.requirementNotAddedForRemovedRequirement",
                                updatedReq.getRequirementName()));
                checkNonconformitiesForRemovedRequirement(updatedSurveillance, null, updatedReq.getNonconformities());
            } else if (existingReq.isPresent()) {
                if (hasRemovedCriteria(updatedReq) && !updatedReq.matches(existingReq.get())) {
                    // if it's an existing requirement with a removed criteria then it can't be edited
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotEditedForRemovedCriteria",
                                    updatedReq.getRequirementName()));
                } else if (hasRemovedRequirement(updatedReq) && !updatedReq.matches(existingReq.get())) {
                    // if it's an existing requirement with transparency then it can't be edited
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotEditedForRemovedRequirement",
                                    updatedReq.getRequirementName()));
                }
                checkNonconformitiesForRemovedCriteria(
                        updatedSurveillance, existingReq.get().getNonconformities(), updatedReq.getNonconformities());
                checkNonconformitiesForRemovedRequirement(
                        updatedSurveillance, existingReq.get().getNonconformities(), updatedReq.getNonconformities());
            }
        }
    }

    /**
     * Look for new or updated nonconformities that reference removed criteria. Add error message if found.
     */
    private void checkNonconformitiesForRemovedCriteria(Surveillance updatedSurveillance,
            List<SurveillanceNonconformity> existingSurvNonconformities,
            List<SurveillanceNonconformity> updatedSurvNonconformities) {
        for (SurveillanceNonconformity updatedNonconformity : updatedSurvNonconformities) {
            // look for an existing nonconformity that matches the updated nonconformity
            // and check for removed criteria and/or updates to the nonconformity
            Optional<SurveillanceNonconformity> existingNonconformity = existingSurvNonconformities == null
                    ? Optional.empty() : existingSurvNonconformities.stream()
                    .filter(existingSurvNonconformity -> doNonconformitiesMatch(updatedNonconformity, existingSurvNonconformity))
                    .findFirst();

            if (!existingNonconformity.isPresent() && hasRemovedCriteria(updatedNonconformity)) {
                // if it's a new nonconformity it can't have any removed criteria
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedCriteria",
                                updatedNonconformity.getNonconformityTypeName()));
            } else if (existingNonconformity.isPresent()
                    && hasRemovedCriteria(updatedNonconformity)
                    && !updatedNonconformity.matches(existingNonconformity.get())) {
                // if it's an existing nonconformity with a removed criteria then it can't be edited
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
            // look for an existing nonconformity that matches the updated nonconformity
            // and check for removed transparency and/or updates to the nonconformity
            Optional<SurveillanceNonconformity> existingNonconformity = existingSurvNonconformities == null
                    ? Optional.empty() : existingSurvNonconformities.stream()
                    .filter(existingSurvNonconformity -> doNonconformitiesMatch(updatedNonconformity, existingSurvNonconformity))
                    .findFirst();

            if (!existingNonconformity.isPresent() && hasRemovedCriteria(updatedNonconformity)) {
                // if it's a new nonconformity it can't have any removed criteria
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedCriteria",
                                updatedNonconformity.getNonconformityTypeName()));
            } else if (!existingNonconformity.isPresent() && hasRemovedRequirement(updatedNonconformity)) {
                // if it's a new nonconformity it can't have transparency
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotAddedForRemovedRequirement",
                                updatedNonconformity.getNonconformityTypeName()));
            } else if (existingNonconformity.isPresent()
                    && hasRemovedCriteria(updatedNonconformity)
                    && !updatedNonconformity.matches(existingNonconformity.get())) {
                // if it's an existing nonconformity with a removed criteria then it can't be edited
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotEditedForRemovedCriteria",
                                updatedNonconformity.getNonconformityType()));
            } else if (existingNonconformity.isPresent()
                    && hasRemovedRequirement(updatedNonconformity)
                    && !updatedNonconformity.matches(existingNonconformity.get())) {
                // if it's an existing nonconformity with transparency then it can't be edited
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
                String requirementCriteria = criterionService.coerceToCriterionNumberFormat(req.getRequirement());
                CertificationCriterionDTO criterion = criterionDao.getAllByNumber(requirementCriteria).get(0);
                // TODO Fix this as part of OCD-3220
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
            String nonconformityType = criterionService.coerceToCriterionNumberFormat(nonconformity.getNonconformityType());
            List<CertificationCriterionDTO> criteria = criterionDao.getAllByNumber(nonconformityType);
            // TODO Fix this as part of OCD-3220
            if (criteria != null && criteria.size() > 0) {
                CertificationCriterionDTO criterion = criteria.get(0);
                if (criterion != null && criterion.getRemoved() != null
                        && criterion.getRemoved().booleanValue()) {
                    return true;
                }
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
