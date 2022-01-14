package gov.healthit.chpl.manager.rules.developer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.manager.rules.ValidationRule;

public class DeveloperStatusEventsValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean getErrorMessages(DeveloperValidationContext context) {
        List<String> errors = validateDeveloperStatusEvents(context.getDeveloperDTO().getStatusEvents(), context);
        if (!errors.isEmpty()) {
            getMessages().addAll(errors);
            return false;
        }
        return true;
    }

    private List<String> validateDeveloperStatusEvents(final List<DeveloperStatusEventDTO> statusEvents,
            DeveloperValidationContext context) {
        List<String> errors = new ArrayList<String>();
        if (statusEvents == null || statusEvents.size() == 0) {
            errors.add(context.getErrorMessageUtil().getMessage("developer.status.noCurrent"));
        } else {
            // sort the status events by date
            statusEvents.sort(new DeveloperStatusEventComparator());

            // now that the list is sorted by date, make sure no two statuses
            // next to each other are the same
            Iterator<DeveloperStatusEventDTO> iter = statusEvents.iterator();
            DeveloperStatusEventDTO prev = null, curr = null;
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
                        && prev.getStatus().getStatusName().equalsIgnoreCase(curr.getStatus().getStatusName())) {
                    errors.add(context.getErrorMessageUtil().getMessage("developer.status.duplicateStatus",
                            prev.getStatus().getStatusName()));
                }
            }
        }
        return errors;
    }

    static class DeveloperStatusEventComparator implements Comparator<DeveloperStatusEventDTO>, Serializable {
        private static final long serialVersionUID = 7816629342251138939L;

        @Override
        public int compare(final DeveloperStatusEventDTO o1, final DeveloperStatusEventDTO o2) {
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
