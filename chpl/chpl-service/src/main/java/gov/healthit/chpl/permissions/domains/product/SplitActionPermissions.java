package gov.healthit.chpl.permissions.domains.product;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productSplitActionPermissions")
public class SplitActionPermissions extends ActionPermissions {

    private List<CertificationStatusType> allowedCertStatuses;

    public SplitActionPermissions() {
        allowedCertStatuses = new ArrayList<CertificationStatusType>();
        allowedCertStatuses.add(CertificationStatusType.Active);
        allowedCertStatuses.add(CertificationStatusType.SuspendedByAcb);
        allowedCertStatuses.add(CertificationStatusType.SuspendedByOnc);
        allowedCertStatuses.add(CertificationStatusType.TerminatedByOnc);
        allowedCertStatuses.add(CertificationStatusType.WithdrawnByAcb);
        allowedCertStatuses.add(CertificationStatusType.WithdrawnByDeveloper);
        allowedCertStatuses.add(CertificationStatusType.WithdrawnByDeveloperUnderReview);
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof ProductDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            try {
                ProductDTO dto = (ProductDTO) obj;
                if (getResourcePermissions().isDeveloperActive(dto.getDeveloperId())) {
                    return doesCurrentUserHaveAccessToAllOfDevelopersListings(dto.getDeveloperId(), allowedCertStatuses);
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
