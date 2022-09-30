package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.NullSafeEvaluator;

@Component("surveillanceRemovedDataComparisonReviewer")
public class SurveillanceRemovedDataComparisonReviewer implements ComparisonReviewer {

    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;

    @Autowired
    public SurveillanceRemovedDataComparisonReviewer(ErrorMessageUtil msgUtil, ResourcePermissions resourcePermissions) {
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
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

            if (!existingReq.isPresent() && hasRemovedRequirementDetailType(updatedReq)) {
                // if it's a new requirement it can't have any removed criteria
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.requirementNotAddedForRemoved",
                                updatedReq.getRequirementDetailType().getFormattedTitle()));
                checkForRemovedNonconformities(updatedSurveillance, null, updatedReq.getNonconformities());
            } else if (existingReq.isPresent()) {
                if (hasRemovedRequirementDetailType(updatedReq) && !updatedReq.matches(existingReq.get())) {
                    // if it's an existing requirement with a removed criteria then it can't be edited
                    updatedSurveillance.getErrorMessages().add(
                            msgUtil.getMessage("surveillance.requirementNotEditedForRemoved",
                                    updatedReq.getRequirementDetailType().getFormattedTitle()));
                }
                checkForRemovedNonconformities(updatedSurveillance, existingReq.get().getNonconformities(), updatedReq.getNonconformities());
            }
        }
    }

    private void checkForRemovedNonconformities(Surveillance updatedSurveillance,
            List<SurveillanceNonconformity> existingSurvNonconformities,
            List<SurveillanceNonconformity> updatedSurvNonconformities) {

        for (SurveillanceNonconformity updatedNonconformity : updatedSurvNonconformities) {
            // look for an existing nonconformity that matches the updated nonconformity
            // and check for removed transparency and/or updates to the nonconformity
            Optional<SurveillanceNonconformity> existingNonconformity =
                    existingSurvNonconformities == null
                    ? Optional.empty()
                    : existingSurvNonconformities.stream()
                            .filter(existingSurvNonconformity -> doNonconformitiesMatch(updatedNonconformity, existingSurvNonconformity))
                            .findFirst();

            if (!existingNonconformity.isPresent() && hasRemovedNonconformityType(updatedNonconformity)) {
                // if it's a new nonconformity it can't have any removed nonconformity
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotAddedForRemoved",
                                updatedNonconformity.getType().getFormattedTitle()));
            } else if (existingNonconformity.isPresent()
                    && hasRemovedNonconformityType(updatedNonconformity)
                    && !updatedNonconformity.matches(existingNonconformity.get())) {
                // if it's an existing nonconformity with a removed nonconformity then it can't be edited
                updatedSurveillance.getErrorMessages().add(
                        msgUtil.getMessage("surveillance.nonconformityNotEditedForRemoved",
                                updatedNonconformity.getType().getFormattedTitle()));
            }
        }
    }

    private boolean doRequirementsMatch(SurveillanceRequirement updatedReq, SurveillanceRequirement existingReq) {
        return updatedReq.getId() != null && existingReq.getId() != null
                && updatedReq.getId().equals(existingReq.getId());
    }

    private boolean doNonconformitiesMatch(SurveillanceNonconformity updatedNonconformity, SurveillanceNonconformity existingNonconformity) {
        return updatedNonconformity.getId() != null && existingNonconformity.getId() != null
                && updatedNonconformity.getId().equals(existingNonconformity.getId());
    }

    private boolean hasRemovedRequirementDetailType(SurveillanceRequirement requirement) {
        return NullSafeEvaluator.eval(() -> requirement.getRequirementDetailType().getRemoved(), false);
    }

    private boolean hasRemovedNonconformityType(SurveillanceNonconformity nonconformity) {
        return NullSafeEvaluator.eval(() -> nonconformity.getType().getRemoved(), false);
    }
}
