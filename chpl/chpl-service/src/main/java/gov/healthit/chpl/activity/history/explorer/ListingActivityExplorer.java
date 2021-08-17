package gov.healthit.chpl.activity.history.explorer;

import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;

import gov.healthit.chpl.activity.history.query.ListingActivityQuery;
import gov.healthit.chpl.dto.ActivityDTO;

public abstract class ListingActivityExplorer {

    public abstract ActivityDTO getActivity(ListingActivityQuery query);

    public void sortNewestActivityFirst(List<ActivityDTO> activities) {
        activities.sort(new Comparator<ActivityDTO>() {
            @Override
            public int compare(ActivityDTO o1, ActivityDTO o2) {
                if (ObjectUtils.allNotNull(o1, o2, o1.getActivityDate(), o2.getActivityDate())) {
                    return o2.getActivityDate().compareTo(o1.getActivityDate());
                }
                return 0;
            }
        });
    }

    public void sortOldestActivityFirst(List<ActivityDTO> activities) {
        activities.sort(new Comparator<ActivityDTO>() {
            @Override
            public int compare(ActivityDTO o1, ActivityDTO o2) {
                if (ObjectUtils.allNotNull(o1, o2, o1.getActivityDate(), o2.getActivityDate())) {
                    return o1.getActivityDate().compareTo(o2.getActivityDate());
                }
                return 0;
            }
        });
    }
}
