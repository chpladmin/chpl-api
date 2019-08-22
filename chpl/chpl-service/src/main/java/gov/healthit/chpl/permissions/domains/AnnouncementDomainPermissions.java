package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.announcement.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.announcement.GetAllIncludingFutureActionPermissions;
import gov.healthit.chpl.permissions.domains.announcement.GetByIdActionPermissions;
import gov.healthit.chpl.permissions.domains.announcement.UpdateActionPermissions;
import gov.healthit.chpl.permissions.domains.announcement.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.announcement.DeleteActionPermissions;

@Component
public class AnnouncementDomainPermissions extends DomainPermissions {
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String GET_ALL = "GET_ALL";
    public static final String GET_BY_ID = "GET_BY_ID";
    public static final String GET_ALL_INCLUDING_FUTURE = "GET_ALL_INCLUDING_FUTURE";

    @Autowired
    public AnnouncementDomainPermissions(
            @Qualifier("announcementCreateActionPermissions") final CreateActionPermissions createActionPermissions,
            @Qualifier("announcementUpdateActionPermissions") final UpdateActionPermissions updateActionPermissions,
            @Qualifier("announcementDeleteActionPermissions") final DeleteActionPermissions deleteActionPermissions,
            @Qualifier("announcementGetAllActionPermissions") final GetAllActionPermissions getAllActionPermissions,
            @Qualifier("announcementGetByIdActionPermissions") final GetByIdActionPermissions getByIdActionPermissions,
            @Qualifier("announcementGetAllIncludingFutureActionPermissions") final GetAllIncludingFutureActionPermissions getAllIncludingFutureActionPermissions) {

        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(GET_BY_ID, getByIdActionPermissions);
        getActionPermissions().put(GET_ALL_INCLUDING_FUTURE, getAllIncludingFutureActionPermissions);
    }
}
