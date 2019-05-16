package gov.healthit.chpl.permissions.domains.filter;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;

@Component("filterCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof FilterDTO)) {
            return false;
        } else {
            User user = AuthUtil.getCurrentUser();
            if (user != null) {
                FilterDTO origFilter = (FilterDTO) obj;
                return origFilter.getUser().getId().equals(AuthUtil.getCurrentUser().getId());
            } else {
                return false;
            }
        }
    }
}
