package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.certificationresults.UpdatePermissions;

@Component
public class CertificationResultsDomainPermissions extends DomainPermissions {
    public static final String UPDATE = "UPDATE";

    public CertificationResultsDomainPermissions(
            @Qualifier("certificationResultsUpdatePermissions") UpdatePermissions updatePermissions) {
        getActionPermissions().put(UPDATE, updatePermissions);
    }
}
