package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("getStandardActivityMetadataActionPermissions")
public class GetStandardMetadataActionPermissions extends ActionPermissions {
  @Override
  public boolean hasAccess() {
    return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
  }

  @Override
  public boolean hasAccess(Object obj) {
    return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
  }
}
