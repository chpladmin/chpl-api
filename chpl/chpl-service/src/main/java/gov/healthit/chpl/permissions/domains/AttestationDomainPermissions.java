package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.attestation.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.attestation.CreateExceptionActionPermissions;
import gov.healthit.chpl.permissions.domains.attestation.GetByDeveloperIdActionPermissions;

@Component
public class AttestationDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String CREATE_EXCEPTION = "CREATE_EXCEPTION";
    public static final String GET_BY_DEVELOPER_ID = "GET_BY_DEVELOPER_ID";

    @Autowired
    public AttestationDomainPermissions(
            @Qualifier("attestationGetByDeveloperIdActionPermissions") GetByDeveloperIdActionPermissions getByDeveloperActionPermissions,
            @Qualifier("attestationCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("attestationCreateExceptionActionPermissions") CreateExceptionActionPermissions createExceptionActionPermissions) {

        getActionPermissions().put(GET_BY_DEVELOPER_ID, getByDeveloperActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(CREATE_EXCEPTION, createExceptionActionPermissions);
    }
}
