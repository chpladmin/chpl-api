package gov.healthit.chpl.manager.rules.version;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.manager.rules.ValidationRule;

@Component
public class VersionValidationFactory {
    public static final String NAME = "NAME";

    @Autowired
    public VersionValidationFactory() {
    }

    public ValidationRule<VersionValidationContext> getRule(String name) {
        switch (name) {
        case NAME:
            return new VersionNameValidation();
        default:
            return null;
        }
    }
}
