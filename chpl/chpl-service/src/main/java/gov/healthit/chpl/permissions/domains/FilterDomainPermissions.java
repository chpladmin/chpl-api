package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.filter.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.filter.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.filter.GetByFilterTypeActionPermissions;
import gov.healthit.chpl.permissions.domains.filter.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.filter.UpdateActionPermissions;

@Component
public class FilterDomainPermissions extends DomainPermissions {
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String GET_BY_FILTER_TYPE = "GET_BY_FILTER_TYPE";
    public static final String CREATE = "CREATE";
    public static final String GET_BY_ID = "GET_BY_ID";

    @Autowired
    public FilterDomainPermissions(
            @Qualifier("filterUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("filterDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("filterCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("filterGetByFilterTypeActionPermissions") GetByFilterTypeActionPermissions getByFilterTypeActionPermissions,
            @Qualifier("filterGetByIdActionPermissions") GetByIdActionPermissions getByIdActionPermissions) {

        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(GET_BY_FILTER_TYPE, getByFilterTypeActionPermissions);
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
    }
}
