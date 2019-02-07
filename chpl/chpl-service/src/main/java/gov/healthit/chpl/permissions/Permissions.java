package gov.healthit.chpl.permissions;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions;
import gov.healthit.chpl.permissions.domains.CertificationResultsDomainPermissions;
import gov.healthit.chpl.permissions.domains.CertifiedProductDomainPermissions;
import gov.healthit.chpl.permissions.domains.CorrectiveActionPlanDomainPermissions;
import gov.healthit.chpl.permissions.domains.DomainPermissions;
import gov.healthit.chpl.permissions.domains.InvitationDomainPermissions;
import gov.healthit.chpl.permissions.domains.PendingCertifiedProductDomainPermissions;
import gov.healthit.chpl.permissions.domains.PendingSurveillanceDomainPermissions;
import gov.healthit.chpl.permissions.domains.SurveillanceDomainPermissions;

@Component
public class Permissions {
    public static final String PENDING_SURVEILLANCE = "PENDING_SURVEILLANCE";
    public static final String CERTIFICATION_RESULTS = "CERTIFICATION_RESULTS";
    public static final String CERTIFIED_PRODUCT = "CERTIFIED_PRODUCT";
    public static final String CORRECTIVE_ACTION_PLAN = "CORRECTIVE_ACTION_PLAN";
    public static final String INVITATION = "INVITATION";
    public static final String PENDING_CERTIFIED_PRODUCT = "PENDING_CERTIFIED_PRODUCT";
    public static final String SURVEILLANCE = "SURVEILLANCE";
    public static final String CERTIFICATION_BODY = "CERTIFICATION_BODY";
    public static final String SCHEDULER = "SCHEDULER";

    private Map<String, DomainPermissions> domainPermissions = new HashMap<String, DomainPermissions>();

    @Autowired
    public Permissions(final PendingSurveillanceDomainPermissions pendingSurveillanceDomainPermissions,

            final CertificationResultsDomainPermissions certificationResultsDomainPermissions,
            final CertifiedProductDomainPermissions certifiedProductDomainPermissions,
            final CorrectiveActionPlanDomainPermissions correctiveActionPlanDomainPermissions,
            final InvitationDomainPermissions invitationDomainPermissions,
            final PendingCertifiedProductDomainPermissions pendingCertifiedProductDomainPermissions,
            final SurveillanceDomainPermissions surveillanceDomainPermissions,
            final CertificationBodyDomainPermissions certificationBodyDomainPermissions) {

        domainPermissions.put(PENDING_SURVEILLANCE, pendingSurveillanceDomainPermissions);
        domainPermissions.put(CERTIFICATION_RESULTS, certificationResultsDomainPermissions);
        domainPermissions.put(CERTIFIED_PRODUCT, certifiedProductDomainPermissions);
        domainPermissions.put(CORRECTIVE_ACTION_PLAN, correctiveActionPlanDomainPermissions);
        domainPermissions.put(INVITATION, invitationDomainPermissions);
        domainPermissions.put(PENDING_CERTIFIED_PRODUCT, pendingCertifiedProductDomainPermissions);
        domainPermissions.put(SURVEILLANCE, surveillanceDomainPermissions);
        domainPermissions.put(CERTIFICATION_BODY, certificationBodyDomainPermissions);
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
