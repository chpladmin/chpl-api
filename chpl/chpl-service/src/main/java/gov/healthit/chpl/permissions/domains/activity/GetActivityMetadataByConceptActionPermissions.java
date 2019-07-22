package gov.healthit.chpl.permissions.domains.activity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("activityGetActivityMetadataByConceptActionPermissions")
public class GetActivityMetadataByConceptActionPermissions extends ActionPermissions {
    List<ActivityConcept> availableConcepts = new ArrayList<ActivityConcept>() {
        private static final long serialVersionUID = 6936194537061096863L;
        {
            add(ActivityConcept.CERTIFIED_PRODUCT);
            add(ActivityConcept.PRODUCT);
            add(ActivityConcept.DEVELOPER);
            add(ActivityConcept.VERSION);
            add(ActivityConcept.CORRECTIVE_ACTION_PLAN);
        }
    };

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ActivityConcept)) {
            return false;
        } else {
            ActivityConcept concept = (ActivityConcept) obj;
            return availableConcepts.contains(concept);
        }
    }

}
