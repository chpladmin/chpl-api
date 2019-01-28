package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.certifiedproduct.CleanDataActionPermissions;
import gov.healthit.chpl.permissions.domains.certifiedproduct.CreateFromPendingActionPermissions;
import gov.healthit.chpl.permissions.domains.certifiedproduct.UpdateActionPermissions;

@Component
public class CertifiedProductDomainPermissions extends DomainPermissions {
    public static final String CREATE_FROM_PENDING = "CREATE_FROM_PENDING";
    public static final String CLEAN_DATA = "CLEAN_DATA";
    public static final String UPDATE = "UPDATE";

    public CertifiedProductDomainPermissions(
            @Qualifier("certifiedProductCreateFromPendingActionPermissions") CreateFromPendingActionPermissions createFromPendingActionPermissions,
            @Qualifier("certifiedProductCleanDataActionPermissions") CleanDataActionPermissions cleanDataActionPermissions,
            @Qualifier("certifiedProductUpdateActionPermissions") UpdateActionPermissions updateActionPermissions) {
        getActionPermissions().put(CREATE_FROM_PENDING, createFromPendingActionPermissions);
        getActionPermissions().put(CLEAN_DATA, cleanDataActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
    }
}
