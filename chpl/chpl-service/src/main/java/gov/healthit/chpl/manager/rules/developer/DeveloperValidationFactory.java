package gov.healthit.chpl.manager.rules.developer;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class DeveloperValidationFactory {
    public static final String NAME = "NAME";
    public static final String WEBSITE_REQUIRED = "WEBSITE";
    public static final String WEBSITE_WELL_FORMED = "WEBSITE_WELL_FORMED";
    public static final String CONTACT = "CONTACT";
    public static final String ADDRESS = "ADDRESS";
    public static final String TRANSPARENCY_ATTESTATION = "TRANSPARENCY_ATTESTATION";
    public static final String STATUS_EVENTS = "STATUS_EVENTS";

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
        default:
            return null;
        }
    }
}
