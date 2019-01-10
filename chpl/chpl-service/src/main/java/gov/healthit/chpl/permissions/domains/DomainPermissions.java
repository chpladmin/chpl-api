package gov.healthit.chpl.permissions.domains;

import java.util.HashMap;
import java.util.Map;

public class DomainPermissions {
    private Map<String, ActionPermissions> actionPermissions = new HashMap<String, ActionPermissions>();

    public boolean hasAccess(String action) {
        if (getActionPermissions().containsKey(action)) {
            return getActionPermissions().get(action).hasAccess();
        } else {
            return false;
        }
    }

    public boolean hasAccess(String action, Object obj) {
        if (getActionPermissions().containsKey(action)) {
            return getActionPermissions().get(action).hasAccess();
        } else {
            return false;
        }
    }

    public Map<String, ActionPermissions> getActionPermissions() {
        return actionPermissions;
    }

    public void setActionPermissions(Map<String, ActionPermissions> actionPermissions) {
        this.actionPermissions = actionPermissions;
    }


}
