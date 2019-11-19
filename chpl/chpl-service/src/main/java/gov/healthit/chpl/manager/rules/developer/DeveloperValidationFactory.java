package gov.healthit.chpl.manager.rules.developer;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissions;

@Component
public class DeveloperValidationFactory {
    public static final String NAME = "NAME";
    public static final String WEBSITE_REQUIRED = "WEBSITE";
    public static final String WEBSITE_WELL_FORMED = "WEBSITE_WELL_FORMED";
    public static final String CONTACT = "CONTACT";
    public static final String ADDRESS = "ADDRESS";
    public static final String TRANSPARENCY_ATTESTATION = "TRANSPARENCY_ATTESTATION";
    public static final String STATUS_EVENTS = "STATUS_EVENTS";
    public static final String EDIT_TRANSPARENCY_ATTESTATION = "EDIT_TRANSPARENCY_ATTESTATION";
    public static final String HAS_STATUS = "HAS_STATUS";
    public static final String STATUS_MISSING_BAN_REASON = "STATUS_MISSING_BAN_REASON";

    private FF4j ff4j;
    private ResourcePermissions resourcePermissions;

    @Autowired
    public DeveloperValidationFactory(final FF4j ff4j, final ResourcePermissions resourcePermissions) {
        this.ff4j = ff4j;
        this.resourcePermissions = resourcePermissions;
    }

    public ValidationRule<DeveloperValidationContext> getRule(String name) {
        switch (name) {
        case NAME:
            return new DeveloperNameValidation();
        case WEBSITE_REQUIRED:
            return new DeveloperWebsiteRequiredValidation();
        case WEBSITE_WELL_FORMED:
            return new DeveloperWebsiteWellFormedValidation();
        case CONTACT:
            return new DeveloperContactValidation();
        case ADDRESS:
            return new DeveloperAddressValidation();
        case TRANSPARENCY_ATTESTATION:
            return new DeveloperTransparencyAttestationValidation();
        case STATUS_EVENTS:
            return new DeveloperStatusEventsValidation();
        case EDIT_TRANSPARENCY_ATTESTATION:
            return new DeveloperEditTransparencyAttestationValidation(ff4j, resourcePermissions);
        case HAS_STATUS:
            return new DeveloperHasStatusValidation();
        case STATUS_MISSING_BAN_REASON:
            return new DeveloperStatusMissingBanReasonValidation();
        default:
            return null;
        }
    }
}
