package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.fuzzyMatch.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.fuzzyMatch.UpdateActionPermissions;

@Component
public class FuzzyMatchPermissions extends DomainPermissions {
    public static final String UPDATE = "UPDATE";
    public static final String GET_ALL = "GET_ALL";

    @Autowired
    public FuzzyMatchPermissions(
            @Qualifier("fuzzyMatchUpdateActionPermissions") final UpdateActionPermissions updateActionPermissions,
            @Qualifier("fuzzyMatchGetAllActionPermissions") final GetAllActionPermissions getAllActionPermissions) {

        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
    }
}
