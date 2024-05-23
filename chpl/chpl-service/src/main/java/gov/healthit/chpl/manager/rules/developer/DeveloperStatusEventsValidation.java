package gov.healthit.chpl.manager.rules.developer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import gov.healthit.chpl.developer.DeveloperStatusEventComparator;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.util.DateUtil;

public class DeveloperStatusEventsValidation extends ValidationRule<DeveloperValidationContext> {
    @Override
    public boolean isValid(DeveloperValidationContext context) {
        List<String> errors = validateDeveloperStatusEvents(context.getDeveloper().getStatuses(), context);
        if (!errors.isEmpty()) {
            getMessages().addAll(errors);
            return false;
        }
        return true;
    }

    private List<String> validateDeveloperStatusEvents(List<DeveloperStatusEvent> statusEvents, DeveloperValidationContext context) {
        List<String> errors = new ArrayList<String>();
        if (!CollectionUtils.isEmpty(statusEvents)) {
            // sort the status events by date
            statusEvents.sort(new DeveloperStatusEventComparator());
            errors.addAll(statusEventsDatesOverlap(statusEvents, context));
        }
        return errors;
    }

    private List<String> statusEventsDatesOverlap(List<DeveloperStatusEvent> statusEvents, DeveloperValidationContext context) {
        List<String> errors = new ArrayList<String>();
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
                    && DateUtil.datesOverlap(prev.getStartDay(), prev.getEndDay(), curr.getStartDay(), curr.getEndDay())) {
                errors.add(context.getErrorMessageUtil().getMessage("developer.status.datesOverlap",
                        prev.getStatus().getName()));
            }
        }
        return errors;
    }
}
