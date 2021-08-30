package gov.healthit.chpl.validation.surveillance.reviewer;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("surveillanceDeprecatedFieldReviewer")
public class DeprecatedFieldReviewer implements ComparisonReviewer {

    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeprecatedFieldReviewer(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    @Override
    public void review(Surveillance existingSurveillance, Surveillance updatedSurveillance) {
        checkSurveillanceDeprecatedFieldUsage(existingSurveillance, updatedSurveillance);

        for (SurveillanceRequirement updatedReq : updatedSurveillance.getRequirements()) {
            Optional<SurveillanceRequirement> existingReq = existingSurveillance.getRequirements().stream()
                    .filter(existingSurvReq -> doRequirementsMatch(updatedReq, existingSurvReq))
                    .findFirst();

            if (existingReq.isPresent()) {
                checkNonconformityDeprecatedFieldUsage(
                        updatedSurveillance, existingReq.get().getNonconformities(), updatedReq.getNonconformities());
            }
        }
    }

    private void checkSurveillanceDeprecatedFieldUsage(Surveillance existingSurveillance, Surveillance updatedSurveillance) {
        if (!Objects.equals(existingSurveillance.getStartDate(),updatedSurveillance.getStartDate())) {
            updatedSurveillance.getWarningMessages().add(
                    msgUtil.getMessage("deprecated.field.update", "startDate", "startDay"));
        }

        if (!Objects.equals(existingSurveillance.getEndDate(),updatedSurveillance.getEndDate())) {
            updatedSurveillance.getWarningMessages().add(
                    msgUtil.getMessage("deprecated.field.update", "endDate", "endDay"));
        }
    }

    private void checkNonconformityDeprecatedFieldUsage(Surveillance updatedSurveillance,
            List<SurveillanceNonconformity> existingSurvNonconformities,
            List<SurveillanceNonconformity> updatedSurvNonconformities) {
        for (SurveillanceNonconformity updatedNonconformity : updatedSurvNonconformities) {
            // look for an existing nonconformity that matches the updated nonconformity
            Optional<SurveillanceNonconformity> existingNonconformityOpt = existingSurvNonconformities == null
                    ? Optional.empty() : existingSurvNonconformities.stream()
                    .filter(existingSurvNonconformity -> doNonconformitiesMatch(updatedNonconformity, existingSurvNonconformity))
                    .findFirst();

            if (existingNonconformityOpt.isPresent()) {
                SurveillanceNonconformity existingNonconformity = existingNonconformityOpt.get();
                if (!Objects.equals(existingNonconformity.getCapApprovalDate(), updatedNonconformity.getCapApprovalDate())) {
                    updatedSurveillance.getWarningMessages().add(
                            msgUtil.getMessage("deprecated.field.update", "capApprovalDate", "capApprovalDay"));
                }
                if (!Objects.equals(existingNonconformity.getCapEndDate(), updatedNonconformity.getCapEndDate())) {
                    updatedSurveillance.getWarningMessages().add(
                            msgUtil.getMessage("deprecated.field.update", "capEndDate", "capEndDay"));
                }
                if (!Objects.equals(existingNonconformity.getCapMustCompleteDate(), updatedNonconformity.getCapMustCompleteDate())) {
                    updatedSurveillance.getWarningMessages().add(
                            msgUtil.getMessage("deprecated.field.update", "capMustCompleteDate", "capMustCompleteDay"));
                }
                if (!Objects.equals(existingNonconformity.getCapStartDate(), updatedNonconformity.getCapStartDate())) {
                    updatedSurveillance.getWarningMessages().add(
                            msgUtil.getMessage("deprecated.field.update", "capStartDate", "capStartDay"));
                }
                if (!Objects.equals(existingNonconformity.getDateOfDetermination(), updatedNonconformity.getDateOfDetermination())) {
                    updatedSurveillance.getWarningMessages().add(
                            msgUtil.getMessage("deprecated.field.update", "dateOfDetermination", "dateOfDeterminationDay"));
                }
            }
        }
    }

    private boolean doRequirementsMatch(SurveillanceRequirement updatedReq, SurveillanceRequirement existingReq) {
        return updatedReq.getId() != null && existingReq.getId() != null
                && updatedReq.getId().equals(existingReq.getId());
    }

    private boolean doNonconformitiesMatch(SurveillanceNonconformity updatedNonconformity,
            SurveillanceNonconformity existingNonconformity) {
        return updatedNonconformity.getId() != null && existingNonconformity.getId() != null
                && updatedNonconformity.getId().equals(existingNonconformity.getId());
    }
}
