package gov.healthit.chpl.permissions.domains.product;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("productSplitActionPermissions")
public class SplitActionPermissions extends ActionPermissions {
    private ErrorMessageUtil msgUtil;
    private List<CertificationStatusType> allowedCertStatuses;

    @Autowired
    public SplitActionPermissions(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
        allowedCertStatuses = new ArrayList<CertificationStatusType>();
        allowedCertStatuses.add(CertificationStatusType.Active);
        allowedCertStatuses.add(CertificationStatusType.SuspendedByAcb);
        allowedCertStatuses.add(CertificationStatusType.SuspendedByOnc);
        allowedCertStatuses.add(CertificationStatusType.TerminatedByOnc);
        allowedCertStatuses.add(CertificationStatusType.WithdrawnByAcb);
        allowedCertStatuses.add(CertificationStatusType.WithdrawnByDeveloper);
        allowedCertStatuses.add(CertificationStatusType.WithdrawnByDeveloperUnderReview);
        allowedCertStatuses.add(CertificationStatusType.Retired);
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof Product)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Product product = (Product) obj;
            if (!getResourcePermissions().isDeveloperActive(product.getOwner().getId())) {
                //ACB can never split product if developer is not active
                return false;
            } else if (!doesCurrentUserHaveAccessToAllOfDevelopersListings(product.getOwner().getId(), allowedCertStatuses)) {
                //ACB can only split product if developer is active and all non-retired
                //listings owned by the developer belong to the user's ACB
                throw new AccessDeniedException(msgUtil.getMessage("product.split.notAllowedMultipleAcbs"));
            }
            return true;
        } else {
            return false;
        }
    }
}
