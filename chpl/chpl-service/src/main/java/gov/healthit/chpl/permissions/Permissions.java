package gov.healthit.chpl.permissions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.DomainPermissions;
import gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions;
import gov.healthit.chpl.permissions.domains.SchedulerDomainPermissions;

@Component
public class Permissions {
    public static final String PENDING_SURVEILLANCE = "PENDING_SURVEILLANCE";
    public static final String SCHEDULER = "SCHEDULER";

    private Map<String, DomainPermissions> domainPermissions = new HashMap<String, DomainPermissions>();

    @Autowired
    public Permissions(final PendingSurveillanceDomainPermissions pendingSurveillanceDomainPermissions,
            final SchedulerDomainPermissions schedulerDomainPermissions) {
        domainPermissions.put(PENDING_SURVEILLANCE, pendingSurveillanceDomainPermissions);
        domainPermissions.put(SCHEDULER, schedulerDomainPermissions);
    }

    public boolean hasAccess(final String domain, final String action) {
        if (domainPermissions.containsKey(domain)) {
            return domainPermissions.get(domain).hasAccess(action);
        } else {
            return false;
        }
    }

    public boolean hasAccess(final String domain, final String action, final Object obj) {
        if (domainPermissions.containsKey(domain)) {
            return domainPermissions.get(domain).hasAccess(action, obj);
        } else {
            return false;
        }
    }
}