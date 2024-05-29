package gov.healthit.chpl.permissions.domains.developer;

import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.developer.search.csv.DeveloperCsvHeadingService;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("developerGetCsvHeadingsActionPermissions")
public class GetCsvHeadingsActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        String heading = null;
        if (obj instanceof String) {
            heading = (String) obj;
        }
        if (heading == null) {
            return false;
        }

        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()) {
            return true;
        }
        //user is developer or anonymous
        return Stream.of(DeveloperCsvHeadingService.ANONYMOUS_DEVELOPER_HEADINGS).toList().contains(heading);
    }

}
