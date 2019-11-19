package gov.healthit.chpl.permissions.domains.activity;

import gov.healthit.chpl.domain.activity.ApiKeyManagementActivityMetadata;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import org.springframework.stereotype.Component;

@Component("activityGetApiKeyManagementActivityMetadataActionPermissions")
public class GetApiKeyManagementActivityMetadataActionPermissions extends ActionPermissions {
  @Override
  public boolean hasAccess() {
    return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
  }

  @Override
  public boolean hasAccess(final Object obj) {
    if (!(obj instanceof ApiKeyManagementActivityMetadata)) {
      return false;
    } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
      return true;
    } else {
      return false;
    }
  }
}
