package gov.healthit.chpl.manager.rules.developer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperStatusEventsValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        List<String> errors = validateDeveloperStatusEvents(context.getDeveloper().getStatusEvents(), context);
        if (!errors.isEmpty()) {
            getMessages().addAll(errors);
            return false;
        }
        return true;
    }

    private List<String> validateDeveloperStatusEvents(List<DeveloperStatusEvent> statusEvents,
            DeveloperValidationContext context) {
        List<String> errors = new ArrayList<String>();
        if (statusEvents == null || statusEvents.size() == 0) {
            errors.add(context.getErrorMessageUtil().getMessage("developer.status.noCurrent"));
        } else {
            // sort the status events by date
            statusEvents.sort(new DeveloperStatusEventComparator());

            // now that the list is sorted by date, make sure the first status is 'Active'
            DeveloperStatusEvent firstStatusEvent = statusEvents.iterator().next();
            if (!firstStatusEvent.getStatus().getStatus().equals(DeveloperStatusType.Active.getName())) {
                errors.add(context.getErrorMessageUtil().getMessage("developer.status.notActiveFirst",
                        firstStatusEvent.getStatus().getStatus()));
            }

            // now that the list is sorted by date, make sure no two statuses
            // next to each other are the same
            Iterator<DeveloperStatusEvent> iter = statusEvents.iterator();
            DeveloperStatusEvent prev = null, curr = null;
            while (iter.hasNext()) {
                if (prev == null) {
                    prev = iter.next();
                } else if (curr == null) {
                    curr = iter.next();
                } else {
                    prev = curr;
                    curr = iter.next();
                }

                if (prev != null && curr != null
                        && prev.getStatus().getStatus().equalsIgnoreCase(curr.getStatus().getStatus())) {
                    errors.add(context.getErrorMessageUtil().getMessage("developer.status.duplicateStatus",
                            prev.getStatus().getStatus()));
                }
            }
        }
        return errors;
    }

    static class DeveloperStatusEventComparator implements Comparator<DeveloperStatusEvent>, Serializable {
        private static final long serialVersionUID = 7816629342251138939L;

        @Override
        public int compare(final DeveloperStatusEvent o1, final DeveloperStatusEvent o2) {
            if (o1 != null && o2 != null) {
                // neither are null, compare the dates
                return o1.getStatusDate().compareTo(o2.getStatusDate());
            } else if (o1 == null && o2 != null) {
                return -1;
            } else if (o1 != null && o2 == null) {
                return 1;
            } else { // o1 and o2 are both null
                return 0;
            }
        }
    }
}
