package gov.healthit.chpl.permissions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.DomainPermissions;
import gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions;

@Component
public class Permissions {
    public static final String PENDING_SURVEILLANCE = "PENDING_SURVEILLANCE";

    private Map<String, DomainPermissions> domainPermissions = new HashMap<String, DomainPermissions>();

    @Autowired
    public Permissions(PendingSurveillanceDomainPermissions pendingSurveillanceDomainPermissions) {
        domainPermissions.put(PENDING_SURVEILLANCE, pendingSurveillanceDomainPermissions);
    }

    public boolean hasAccess(String domain, String action) {
        if (domainPermissions.containsKey(domain)) {
            return domainPermissions.get(domain).hasAccess(action);
        } else {
            return false;
        }
    }

    public boolean hasAccess(String domain, String action, Object obj) {
        if (domainPermissions.containsKey(domain)) {
            return domainPermissions.get(domain).hasAccess(action, obj);
        } else {
            return false;
        }
    }
}
