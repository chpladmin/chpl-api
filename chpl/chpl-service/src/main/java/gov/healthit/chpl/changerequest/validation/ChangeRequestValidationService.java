package gov.healthit.chpl.changerequest.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    public List<String> validate(ChangeRequestValidationContext context) {
        return runValidations(gatherValidations(context), context);
    }

    private List<ValidationRule<ChangeRequestValidationContext>> gatherValidations(ChangeRequestValidationContext context) {
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
                new AttestationValidation()));
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
                new ChangeRequestCreateValidation()));
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getUpdateValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>(Arrays.asList(
                new RoleAcbHasMultipleCertificationBodiesValidation(),
                new DeveloperActiveValidation(),
                new CurrentStatusValidation(),
                new ChangeRequestNotUpdatableDueToStatusValidation(),
                new CommentRequiredValidation()));
    }

    private List<String> runValidations(List<ValidationRule<ChangeRequestValidationContext>> rules, ChangeRequestValidationContext context) {
        try {
            List<String> errorMessages = new ArrayList<String>();
            for (ValidationRule<ChangeRequestValidationContext> rule : rules) {
                if (rule != null && !rule.isValid(context)) {
                    errorMessages.addAll(rule.getMessages());
                }
            }
            return errorMessages;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
