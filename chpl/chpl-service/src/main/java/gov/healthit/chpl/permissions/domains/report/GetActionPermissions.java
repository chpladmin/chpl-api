package gov.healthit.chpl.permissions.domains.report;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.report.ReportMetadata;

@Component("reportMetadataGetActionPermissions")
public class GetActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ReportMetadata)) {
            return false;
        }
        ReportMetadata report = (ReportMetadata) obj;
        if (CollectionUtils.isEmpty(report.getRoleNames())) {
            return true;
        }
        return getResourcePermissions().doesUserHaveRole(report.getRoleNames());
    }

}
