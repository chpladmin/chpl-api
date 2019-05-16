package gov.healthit.chpl.permissions.domains.filter;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;

@Component("filterGetByFilterTypeActionPermissions")
public class GetByFilterTypeActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof FilterDTO)) {
            return false;
        } else {
            FilterDTO origFilter = (FilterDTO) obj;
            if (AuthUtil.getCurrentUser() == null) {
                return false;
            } else {
                return origFilter.getUser().getId().equals(AuthUtil.getCurrentUser().getId());
            }
        }
    }

}
