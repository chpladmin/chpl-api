package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.validation.attestation.AttestationResponseValidation;
import gov.healthit.chpl.changerequest.validation.attestation.AttestationValidation;
import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestValidationService {
    private Long developerDemographicsChangeRequestTypeId;
    private Long attestationChangeRequestTypeId;

    @Autowired
    public ChangeRequestValidationService(
            @Value("${changerequest.developerDemographics}") Long developerDemographicsChangeRequestTypeId,
            @Value("${changerequest.attestation}") Long attestationChangeRequestTypeId) {

        this.developerDemographicsChangeRequestTypeId = developerDemographicsChangeRequestTypeId;
        this.attestationChangeRequestTypeId = attestationChangeRequestTypeId;
    }

    public List<String> getErrorMessages(ChangeRequestValidationContext context) {
        return runValidations(gatherErrorValidations(context), context);
    }

    public List<String> getWarningMessages(ChangeRequestValidationContext context) {
        return runValidations(gatherWarningValidations(context), context);
    }

    private List<ValidationRule<ChangeRequestValidationContext>> gatherErrorValidations(ChangeRequestValidationContext context) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();

        if (isNewChangeRequest(context)) {
            rules.addAll(getCreateValidations());
        } else {
            rules.addAll(getUpdateValidations());
        }

        if (context.getNewChangeRequest().getChangeRequestType().getId().equals(developerDemographicsChangeRequestTypeId)) {
            rules.addAll(getDeveloperDetailsValidations());
        } else if (context.getNewChangeRequest().getChangeRequestType().getId().equals(attestationChangeRequestTypeId)) {
            rules.addAll(getAttestationValidations());
        }

        return rules;
    }

    private List<ValidationRule<ChangeRequestValidationContext>> gatherWarningValidations(ChangeRequestValidationContext context) {
        List<ValidationRule<ChangeRequestValidationContext>> rules = new ArrayList<ValidationRule<ChangeRequestValidationContext>>();

        if (!isNewChangeRequest(context)
            && context.getNewChangeRequest().getChangeRequestType().getId().equals(attestationChangeRequestTypeId)) {
                rules.addAll(getAttestationUpdateValidations());
        }

        return rules;
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getDeveloperDetailsValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>(Arrays.asList(
                new SelfDeveloperValidation(),
                new DemographicsValidation(),
                new ContactValidation(),
                new WebsiteValidation()));
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getAttestationValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>(Arrays.asList(
                new AttestationValidation()));
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getAttestationUpdateValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>(List.of(
                new AttestationResponseValidation()));
    }

    private Boolean isNewChangeRequest(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest() == null;
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getCreateValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>(Arrays.asList(
                new ChangeRequestTypeValidation(),
                new ChangeRequestTypeInProcessValidation(),
                new DeveloperExistenceValidation(),
                new DeveloperActiveValidation(),
                //TODO: make sure the passed-in attestationPeriod for the CR is the same as the currently submittable attestation period for that developer
                new ChangeRequestCreateValidation()));
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getUpdateValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>(Arrays.asList(
                new RoleAcbHasMultipleCertificationBodiesValidation(),
                new DeveloperActiveValidation(),
                new CurrentStatusValidation(),
                new ChangeRequestNotUpdatableDueToStatusValidation(),
                //TODO: make sure the passed-in attestationPeriod for the CR is the same as the currently submittable attestation period for that developer
                new CommentRequiredValidation()));
    }

    private List<String> runValidations(List<ValidationRule<ChangeRequestValidationContext>> rules, ChangeRequestValidationContext context) {
        try {
            List<String> validationMessages = new ArrayList<String>();
            for (ValidationRule<ChangeRequestValidationContext> rule : rules) {
                if (rule != null && !rule.isValid(context)) {
                    validationMessages.addAll(rule.getMessages());
                }
            }
            return validationMessages;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
