package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("actionGetActivityMetadataByAtlActionPermissions")
public class GetActivityMetadataByAtlActionPermissions extends ActionPermissions {

    @Autowired
    public GetActivityMetadataByAtlActionPermissions() {
    }

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff();
    }
}
