package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.testinglab.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.testinglab.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.testinglab.RetireActionPermissions;
import gov.healthit.chpl.permissions.domains.testinglab.UnretireActionPermissions;
import gov.healthit.chpl.permissions.domains.testinglab.UpdateActionPermissions;

@Component
public class TestingLabDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String RETIRE = "RETIRE";
    public static final String UNRETIRE = "UNRETIRE";
    public static final String GET_ALL = "GET_ALL";

    @Autowired
    public TestingLabDomainPermissions(
            @Qualifier("testingLabCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("testingLabUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("testingLabRetireActionPermissions") RetireActionPermissions retireActionPermissions,
            @Qualifier("testingLabUnretireActionPermissions") UnretireActionPermissions unretireActionPermissions,
            @Qualifier("testingLabGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(RETIRE, retireActionPermissions);
        getActionPermissions().put(UNRETIRE, unretireActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
    }
}
