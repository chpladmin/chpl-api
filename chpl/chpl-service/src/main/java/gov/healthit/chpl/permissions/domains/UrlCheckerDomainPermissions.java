package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.testinglab.urlchecker.CheckActionPermissions;

@Component
public class UrlCheckerDomainPermissions extends DomainPermissions {
    public static final String CHECK = "CHECK";

    @Autowired
    public UrlCheckerDomainPermissions(
            @Qualifier("urlCheckerCheckActionPermissions") CheckActionPermissions checkActionPermissions) {
        getActionPermissions().put(CHECK, checkActionPermissions);
    }
}
