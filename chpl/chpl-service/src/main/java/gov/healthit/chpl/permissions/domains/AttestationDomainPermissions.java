package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.attestation.GetByDeveloperIdActionPermissions;

@Component
public class AttestationDomainPermissions extends DomainPermissions {
    public static final String GET_BY_DEVELOPER_ID = "GET_BY_DEVELOPER_ID";

    @Autowired
    public AttestationDomainPermissions(
            @Qualifier("attestationGetByDeveloperIdActionPermissions") GetByDeveloperIdActionPermissions getByDeveloperActionPermissions) {

        getActionPermissions().put(GET_BY_DEVELOPER_ID, getByDeveloperActionPermissions);
    }
}
