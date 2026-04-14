package gov.healthit.chpl.permissions.domains.changerequest;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestCreateMultipleActionPermissions")
public class CreateMultipleActionPermissions extends ActionPermissions {

    @Autowired
    public CreateMultipleActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof Collection) || !containsOnlyChangeRequests((Collection<?>) obj)) {
                return false;
            } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
                Collection<ChangeRequest> changeRequests = (Collection<ChangeRequest>) obj;
                return changeRequests.stream()
                    .filter(cr -> !isDeveloperValidForCurrentUser(cr.getDeveloper().getId()))
                    .findAny()
                    .isEmpty();
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean containsOnlyChangeRequests(Collection<?> collection) {
        for (Object element : collection) {
            if (!(element instanceof ChangeRequest)) {
                return false;
            }
        }
        return true;
    }
}
