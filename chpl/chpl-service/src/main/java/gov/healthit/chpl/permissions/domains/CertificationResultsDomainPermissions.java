package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.certificationresults.CreatePermissions;
import gov.healthit.chpl.permissions.domains.certificationresults.UpdatePermissions;

@Component
public class CertificationResultsDomainPermissions extends DomainPermissions {
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";

    @Autowired
    public CertificationResultsDomainPermissions(
            @Qualifier("certificationResultsUpdatePermissions") UpdatePermissions updatePermissions,
            @Qualifier("certificationResultsCreatePermissions") CreatePermissions createPermissions) {
        getActionPermissions().put(UPDATE, updatePermissions);
        getActionPermissions().put(CREATE, createPermissions);
    }
}
