package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.qmsStandard.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.qmsStandard.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.qmsStandard.UpdateActionPermissions;

@Component
public class QmsStandardDomainPermissions extends DomainPermissions {

    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";

    @Autowired
    public QmsStandardDomainPermissions(
            @Qualifier("qmsStandardDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("qmsStandardUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("qmsStandardCreateActionPermissions") CreateActionPermissions createActionPermissions) {

        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
    }

}
