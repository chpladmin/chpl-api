package gov.healthit.chpl.manager.rules.developer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;

@Component
public class DeveloperValidationFactory {
    public static final String NAME = "NAME";
    public static final String WEBSITE_REQUIRED = "WEBSITE";
    public static final String WEBSITE_WELL_FORMED = "WEBSITE_WELL_FORMED";
    public static final String CONTACT = "CONTACT";
    public static final String ADDRESS = "ADDRESS";
    public static final String STATUS_EVENTS = "STATUS_EVENTS";

    public static final String NOT_BANNED_OR_SUSPENDED_STATUS = "NOT_BANNED_OR_SUSPENDED_STATUS";
    public static final String STATUS_MISSING_BAN_REASON = "STATUS_MISSING_BAN_REASON";
    public static final String PRIOR_STATUS_BANNED_OR_SUSPENDED = "PRIOR_STATUS_BANNED_OR_SUSPENDED";
    public static final String EDIT_STATUS_HISTORY = "EDIT_STATUS_HISTORY";
    public static final String STATUS_CHANGED = "STATUS_CHANGED";

    private DeveloperDAO developerDao;
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    public DeveloperValidationFactory(DeveloperDAO developerDao, ResourcePermissionsFactory resourcePermissionsFactory) {
        this.developerDao = developerDao;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    public ValidationRule<DeveloperValidationContext> getRule(String name) {
        switch (name) {
        case NAME:
            return new DeveloperNameValidation(developerDao);
        case WEBSITE_REQUIRED:
            return new DeveloperWebsiteRequiredValidation();
        case WEBSITE_WELL_FORMED:
            return new DeveloperWebsiteWellFormedValidation();
        case CONTACT:
            return new DeveloperContactValidation();
        case ADDRESS:
            return new DeveloperAddressValidation();
        case STATUS_EVENTS:
            return new DeveloperStatusEventsValidation();
        case NOT_BANNED_OR_SUSPENDED_STATUS:
            return new DeveloperNotBannedOrSuspendedValidation();
        case STATUS_MISSING_BAN_REASON:
            return new DeveloperStatusMissingBanReasonValidation();
        case PRIOR_STATUS_BANNED_OR_SUSPENDED:
            return new DeveloperPriorStatusBannedOrSuspendedValidation(resourcePermissionsFactory);
        case EDIT_STATUS_HISTORY:
            return new DeveloperEditStatusHistoryValidation(resourcePermissionsFactory);
        case STATUS_CHANGED:
            return new DeveloperStatusChangedValidation(resourcePermissionsFactory);
        default:
            return null;
        }
    }
}
