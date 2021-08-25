package gov.healthit.chpl.permissions.domains;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.svap.CreateActionPermissions;
import gov.healthit.chpl.permissions.domains.svap.DeleteActionPermissions;
import gov.healthit.chpl.permissions.domains.svap.SummaryDownloadActionPermissions;
import gov.healthit.chpl.permissions.domains.svap.UpdateActionPermissions;

@Component
public class SvapDomainPermissions extends DomainPermissions {

    public static final String DELETE = "DELETE";
    public static final String UPDATE = "UPDATE";
    public static final String CREATE = "CREATE";
    public static final String SUMMARY_DOWNLOAD = "SUMMARY_DOWNLOAD";

    @Autowired
    public SvapDomainPermissions(
            @Qualifier("svapDeleteActionPermissions") DeleteActionPermissions deleteActionPermissions,
            @Qualifier("svapUpdateActionPermissions") UpdateActionPermissions updateActionPermissions,
            @Qualifier("svapCreateActionPermissions") CreateActionPermissions createActionPermissions,
            @Qualifier("svapSummaryDownloadActionPermissions") SummaryDownloadActionPermissions summaryDownloadActionPermissions) {

        getActionPermissions().put(DELETE, deleteActionPermissions);
        getActionPermissions().put(UPDATE, updateActionPermissions);
        getActionPermissions().put(CREATE, createActionPermissions);
        getActionPermissions().put(SUMMARY_DOWNLOAD, summaryDownloadActionPermissions);
    }

}
