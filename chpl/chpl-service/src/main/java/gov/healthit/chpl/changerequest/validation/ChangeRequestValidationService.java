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

    private ChangeRequestCreateValidation changeRequestCreateValidation;
    private ChangeRequestDetailsUpdateValidation changeRequestDetailsUpdateValidation;
    private ChangeRequestTypeValidation changeRequestTypeValidation;
    private CurrentStatusValidation currentStatusValidation;
    private DeveloperExistenceValidation developerExistenceValidation;
    private DeveloperActiveValidation developerActiveValidation;
    private ChangeRequestNotUpdatableDueToStatusValidation changeRequestNotUpdatableDueToStatusValidation;
    private ChangeRequestTypeInProcessValidation changeRequestTypeInProcessValidation;
    private ChangeRequestModificationValidation changeRequestModificationValidation;
    private CommentRequiredValidation commentRequiredValidation;
    private RoleAcbHasMultipleCertificationBodiesValidation roleAcbHasMultipleCertificationBodiesValidation;
    private WebsiteValidation websiteValidation;
    private SelfDeveloperValidation selfDeveloperValidation;
    private AddressValidation addressValidation;
    private ContactValidation contactValidation;

    private Long websiteChangeRequestTypeId;
    private Long developerDetailsChangeRequestTypeId;
    private Long attestationChangeRequestTypeId;


    @Autowired
    public ChangeRequestValidationService(
            ChangeRequestCreateValidation changeRequestCreateValidation,
            ChangeRequestDetailsUpdateValidation changeRequestDetailsUpdateValidation,
            ChangeRequestTypeValidation changeRequestTypeValidation,
            CurrentStatusValidation currentStatusValidation,
            DeveloperExistenceValidation developerExistenceValidation,
            DeveloperActiveValidation developerActiveValidation,
            ChangeRequestNotUpdatableDueToStatusValidation changeRequestNotUpdatableDueToStatusValidation,
            ChangeRequestTypeInProcessValidation changeRequestTypeInProcessValidation,
            ChangeRequestModificationValidation changeRequestModificationValidation,
            CommentRequiredValidation commentRequiredValidation,
            RoleAcbHasMultipleCertificationBodiesValidation roleAcbHasMultipleCertificationBodiesValidation,
            WebsiteValidation websiteValidation,
            SelfDeveloperValidation selfDeveloperValidation,
            AddressValidation addressValidation,
            ContactValidation contactValidation,
            @Value("${changerequest.website}") Long websiteChangeRequestTypeId,
            @Value("${changerequest.developerDetails}") Long developerDetailsChangeRequestTypeId,
            @Value("${changerequest.attestation}") Long attestationChangeRequestTypeId) {

        this.changeRequestCreateValidation = changeRequestCreateValidation;
        this.changeRequestDetailsUpdateValidation = changeRequestDetailsUpdateValidation;
        this.changeRequestTypeValidation = changeRequestTypeValidation;
        this.currentStatusValidation = currentStatusValidation;
        this.developerExistenceValidation = developerExistenceValidation;
        this.developerActiveValidation = developerActiveValidation;
        this.changeRequestNotUpdatableDueToStatusValidation = changeRequestNotUpdatableDueToStatusValidation;
        this.changeRequestTypeInProcessValidation = changeRequestTypeInProcessValidation;
        this.changeRequestModificationValidation = changeRequestModificationValidation;
        this.commentRequiredValidation = commentRequiredValidation;
        this.roleAcbHasMultipleCertificationBodiesValidation = roleAcbHasMultipleCertificationBodiesValidation;
        this.websiteValidation = websiteValidation;
        this.selfDeveloperValidation = selfDeveloperValidation;
        this.addressValidation = addressValidation;
        this.contactValidation = contactValidation;

        this.websiteChangeRequestTypeId = websiteChangeRequestTypeId;
        this.developerDetailsChangeRequestTypeId = developerDetailsChangeRequestTypeId;
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

        if (context.getNewChangeRequest().getChangeRequestType().getId().equals(websiteChangeRequestTypeId)) {
            rules.addAll(getWebsiteValidations());
        } else if (context.getNewChangeRequest().getChangeRequestType().getId().equals(developerDetailsChangeRequestTypeId)) {
            rules.addAll(getDeveloperDetailsValidations());
        } else if (context.getNewChangeRequest().getChangeRequestType().getId().equals(attestationChangeRequestTypeId)) {
            rules.addAll(getAttestationValidations());
        }

        return rules;
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getWebsiteValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>(Arrays.asList(
                websiteValidation));
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getDeveloperDetailsValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>(Arrays.asList(
                selfDeveloperValidation,
                addressValidation,
                contactValidation));
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getAttestationValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>();
    }

    private Boolean isNewChangeRequest(ChangeRequestValidationContext context) {
        return context.getOrigChangeRequest() == null;
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getCreateValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>(Arrays.asList(
                changeRequestTypeValidation,
                changeRequestTypeInProcessValidation,
                developerExistenceValidation,
                developerActiveValidation,
                changeRequestCreateValidation));
    }

    private List<ValidationRule<ChangeRequestValidationContext>> getUpdateValidations() {
        return new ArrayList<ValidationRule<ChangeRequestValidationContext>>(Arrays.asList(
                changeRequestDetailsUpdateValidation,
                roleAcbHasMultipleCertificationBodiesValidation,
                developerActiveValidation,
                currentStatusValidation,
                changeRequestNotUpdatableDueToStatusValidation,
                commentRequiredValidation,
                changeRequestModificationValidation));
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