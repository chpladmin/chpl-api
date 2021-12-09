package gov.healthit.chpl.changerequest.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class ChangeRequestValidationFactory {

    public static final  String CHANGE_REQUEST_TYPE = "CHANGE_REQUEST_TYPE";
    public static final String CHANGE_REQUEST_IN_PROCESS = "CHANGE_REQUEST_IN_PROCESS";
    public static final String DEVELOPER_EXISTENCE = "DEVELOPER_EXISTENCE";
    public static final String DEVELOPER_ACTIVE = "DEVELOPER_ACTIVE";
    public static final String CHANGE_REQUEST_CREATE = "CHANGE_REQUEST_CREATE";
    public static final String CHANGE_REQUEST_DETAILS_UPDATE = "CHANGE_REQUEST_DETAILS_UPDATE";
    public static final String STATUS_TYPE = "STATUS_TYPE";
    public static final String STATUS_NOT_UPDATABLE = "STATUS_NOT_UPDATABLE";
    public static final String COMMENT_REQUIRED = "COMMENT_REQUIRED";
    public static final String MULTIPLE_ACBS = "MULTIPLE_ACBS";
    public static final String WEBSITE_VALID = "WEBSITE_VALID";
    public static final String SELF_DEVELOPER_VALID = "SELF_DEVELOPER_VALID";
    public static final String ADDRESS_VALID = "ADDRESS_VALID";
    public static final String CONTACT_VALID = "CONTACT_VALID";

    private ChangeRequestCreateValidation changeRequestCreateValidation;
    private ChangeRequestDetailsUpdateValidation changeRequestDetailsUpdateValidation;
    private ChangeRequestTypeValidation changeRequestTypeValidation;
    private CurrentStatusValidation currentStatusValidation;
    private DeveloperExistenceValidation developerExistenceValidation;
    private DeveloperActiveValidation developerActiveValidation;
    private ChangeRequestNotUpdatableDueToStatusValidation changeRequestNotUpdatableDueToStatusValidation;
    private ChangeRequestTypeInProcessValidation changeRequestTypeInProcessValidation;
    private CommentRequiredValidation commentRequiredValidation;
    private RoleAcbHasMultipleCertificationBodiesValidation roleAcbHasMultipleCertificationBodiesValidation;
    private WebsiteValidation websiteValidation;
    private SelfDeveloperValidation selfDeveloperValidation;
    private AddressValidation addressValidation;
    private ContactValidation contactValidation;

    @Autowired
    public ChangeRequestValidationFactory(
            ChangeRequestCreateValidation changeRequestCreateValidation,
            ChangeRequestDetailsUpdateValidation changeRequestDetailsUpdateValidation,
            ChangeRequestTypeValidation changeRequestTypeValidation,
            CurrentStatusValidation currentStatusValidation,
            DeveloperExistenceValidation developerExistenceValidation,
            DeveloperActiveValidation developerActiveValidation,
            ChangeRequestNotUpdatableDueToStatusValidation changeRequestNotUpdatableDueToStatusValidation,
            ChangeRequestTypeInProcessValidation changeRequestTypeInProcessValidation,
            CommentRequiredValidation commentRequiredValidation,
            RoleAcbHasMultipleCertificationBodiesValidation roleAcbHasMultipleCertificationBodiesValidation,
            WebsiteValidation websiteValidation,
            SelfDeveloperValidation selfDeveloperValidation,
            AddressValidation addressValidation,
            ContactValidation contactValidation) {

        this.changeRequestCreateValidation = changeRequestCreateValidation;
        this.changeRequestDetailsUpdateValidation = changeRequestDetailsUpdateValidation;
        this.changeRequestTypeValidation = changeRequestTypeValidation;
        this.currentStatusValidation = currentStatusValidation;
        this.developerExistenceValidation = developerExistenceValidation;
        this.developerActiveValidation = developerActiveValidation;
        this.changeRequestNotUpdatableDueToStatusValidation = changeRequestNotUpdatableDueToStatusValidation;
        this.changeRequestTypeInProcessValidation = changeRequestTypeInProcessValidation;
        this.commentRequiredValidation = commentRequiredValidation;
        this.roleAcbHasMultipleCertificationBodiesValidation = roleAcbHasMultipleCertificationBodiesValidation;
        this.websiteValidation = websiteValidation;
        this.selfDeveloperValidation = selfDeveloperValidation;
        this.addressValidation = addressValidation;
        this.contactValidation = contactValidation;
    }

    public ValidationRule<ChangeRequestValidationContext> getRule(String name) {
        switch (name) {
        case CHANGE_REQUEST_TYPE:
            return changeRequestTypeValidation;
        case CHANGE_REQUEST_IN_PROCESS:
            return changeRequestTypeInProcessValidation;
        case DEVELOPER_EXISTENCE:
            return developerExistenceValidation;
        case DEVELOPER_ACTIVE:
            return developerActiveValidation;
        case CHANGE_REQUEST_CREATE:
            return changeRequestCreateValidation;
        case CHANGE_REQUEST_DETAILS_UPDATE:
            return changeRequestDetailsUpdateValidation;
        case STATUS_TYPE:
            return currentStatusValidation;
        case STATUS_NOT_UPDATABLE:
            return changeRequestNotUpdatableDueToStatusValidation;
        case COMMENT_REQUIRED:
            return commentRequiredValidation;
        case MULTIPLE_ACBS:
            return roleAcbHasMultipleCertificationBodiesValidation;
        case WEBSITE_VALID:
            return websiteValidation;
        case SELF_DEVELOPER_VALID:
            return selfDeveloperValidation;
        case ADDRESS_VALID:
            return addressValidation;
        case CONTACT_VALID:
            return contactValidation;
        default:
            return null;
        }
    }
}
