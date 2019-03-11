package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.certificationbody.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.RetireActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.UnretireActionPermissions;
import gov.healthit.chpl.permissions.domains.certificationbody.UpdateActionPermissions;

@Component
public class CertificationBodyDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String RETIRE = "RETIRE";
    public static final String UNRETIRE = "UNRETIRE";
    public static final String GET_BY_ID = "GET_BY_ID";

    @Autowired
    public CertificationBodyDomainPermissions(
            @Qualifier("certificationBodyCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("certificationBodyUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("certificationBodyRetireActionPermissions") RetireActionPermissions retireActionPermissions,
            @Qualifier("certificationBodyUnretireActionPermissions") UnretireActionPermissions unretireActionPermissions,
            @Qualifier("certificationBodyGetByIdActionPermissions") GetByIdActionPermissions getByIdActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(RETIRE, retireActionPermissions);
        getActionPermissions().put(UNRETIRE, unretireActionPermissions);
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
    }
}
