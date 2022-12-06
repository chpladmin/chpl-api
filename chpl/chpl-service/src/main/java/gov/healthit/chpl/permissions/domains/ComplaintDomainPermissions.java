package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.complaint.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.complaint.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.complaint.DownloadAllActionPermissions;
import gov.healthit.chpl.permissions.domains.complaint.GetAllActionPermissions;
import gov.healthit.chpl.permissions.domains.complaint.SearchActionPermissions;
import gov.healthit.chpl.permissions.domains.complaint.UpdateActionPermissions;

@Component
public class ComplaintDomainPermissions extends DomainPermissions {
    public static final String SEARCH = "SEARCH";
    public static final String GET_ALL = "GET_ALL";
    public static final String CREATE = "CREATE";
    public static final String UPDATE = "UPDATE";
    public static final String DELETE = "DELETE";
    public static final String DOWNLOAD_ALL = "DOWNLOAD_ALL";

    @Autowired
    public ComplaintDomainPermissions(
            @Qualifier("complaintSearchActionPermissions") SearchActionPermissions searchActionPermissions,
            @Qualifier("complaintGetAllActionPermissions") GetAllActionPermissions getAllActionPermissions,
            @Qualifier("complaintCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("complaintUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("complaintDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("complaintDownloadAllActionPermissions") DownloadAllActionPermissions downloadAllActionPermissions) {
        getActionPermissions().put(SEARCH, searchActionPermissions);
        getActionPermissions().put(GET_ALL, getAllActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(DOWNLOAD_ALL, downloadAllActionPermissions);
    }
}
