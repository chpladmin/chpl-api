package gov.healthit.chpl.permissions.domains.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.FilterDAO;
import gov.healthit.chpl.dto.FilterDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.AuthUtil;

@Component("filterDeleteActionPermissions")
public class DeleteActionPermissions extends ActionPermissions {

    @Autowired
    private FilterDAO filterDAO;

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
            // Get the filterDTO from the DB, to make sure user was not tampered with
            try {
                FilterDTO dto = filterDAO.getById(origFilter.getId());
                return dto.getUser().getId().equals(AuthUtil.getCurrentUser().getId());
            } catch (Exception e) {
                return false;
            }
        }
    }

}
