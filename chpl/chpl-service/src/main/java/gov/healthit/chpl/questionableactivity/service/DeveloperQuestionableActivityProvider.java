package gov.healthit.chpl.questionableactivity.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.manager.impl.DeveloperStatusEventsHelper;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityDeveloper;
import gov.healthit.chpl.util.Util;

@Component
public class DeveloperQuestionableActivityProvider {

    /**
     * Check for QA re: developer name.
     * @param origDeveloper original developer
     * @param newDeveloper new developer
     * @return DTO if developer name changed
     */
     public QuestionableActivityDeveloper checkNameUpdated(Developer origDeveloper, Developer newDeveloper) {

        QuestionableActivityDeveloper activity = null;
        if ((origDeveloper.getName() != null && newDeveloper.getName() == null)
                || (origDeveloper.getName() == null && newDeveloper.getName() != null)
                || !origDeveloper.getName().equals(newDeveloper.getName())) {
            activity = new QuestionableActivityDeveloper();
            activity.setBefore(origDeveloper.getName());
            activity.setAfter(newDeveloper.getName());
        }

        return activity;
    }

    /**
     * Check for QA re: developer status.
     * @param origDeveloper original developer
     * @param newDeveloper new developer
     * @return DTO if current status changed
     */
    public QuestionableActivityDeveloper checkCurrentStatusChanged(Developer origDeveloper, Developer newDeveloper) {

        QuestionableActivityDeveloper activity = null;
        if (origDeveloper.getCurrentStatusEvent() != null && newDeveloper.getCurrentStatusEvent() == null) {
            activity = new QuestionableActivityDeveloper();
            activity.setBefore(origDeveloper.getCurrentStatusEvent().getStatus().getName());
            activity.setAfter(null);
        } else if (origDeveloper.getCurrentStatusEvent() == null && newDeveloper.getCurrentStatusEvent() != null) {
            activity = new QuestionableActivityDeveloper();
            activity.setBefore(null);
            activity.setAfter(newDeveloper.getCurrentStatusEvent().getStatus().getName());
        } else if (origDeveloper.getCurrentStatusEvent() != null
                && newDeveloper.getCurrentStatusEvent() != null
                && origDeveloper.getCurrentStatusEvent().getStatus().getId().longValue() !=
                    newDeveloper.getCurrentStatusEvent().getStatus().getId().longValue()) {
            activity = new QuestionableActivityDeveloper();
            activity.setBefore(origDeveloper.getCurrentStatusEvent().getStatus().getName());
            activity.setAfter(newDeveloper.getCurrentStatusEvent().getStatus().getName());
        }

        return activity;
    }

    /**
     * Check for QA re: developer status history.
     * @param origStatuses original developer status events
     * @param newStatuses new developer status events
     * @return list of added statuses
     */
     public List<QuestionableActivityDeveloper> checkStatusHistoryAdded(List<DeveloperStatusEvent> origStatuses, List<DeveloperStatusEvent> newStatuses) {
         List<QuestionableActivityDeveloper> statusAddedActivities = new ArrayList<QuestionableActivityDeveloper>();

         List<DeveloperStatusEvent> addedStatuses = DeveloperStatusEventsHelper.getAddedEvents(origStatuses, newStatuses);
         for (DeveloperStatusEvent newStatusEvent : addedStatuses) {
             QuestionableActivityDeveloper activity = getQuestionableActivityDeveloper(null, getFormattedStatus(newStatusEvent));
             activity.setReason(newStatusEvent.getReason());
             statusAddedActivities.add(activity);
         }
        return statusAddedActivities;
    }

     /**
     * Check for QA re: developer status history.
     * @param origStatuses original developer status events
     * @param newStatuses new developer status events
     * @return list of added statuses
     */
     public List<QuestionableActivityDeveloper> checkStatusHistoryRemoved(
            List<DeveloperStatusEvent> origStatuses, List<DeveloperStatusEvent> newStatuses) {

        List<QuestionableActivityDeveloper> statusRemovedActivities = new ArrayList<QuestionableActivityDeveloper>();

        List<DeveloperStatusEvent> removedStatuses = DeveloperStatusEventsHelper.getRemovedEvents(origStatuses, newStatuses);
        for (DeveloperStatusEvent newStatusEvent : removedStatuses) {
            QuestionableActivityDeveloper activity = getQuestionableActivityDeveloper(getFormattedStatus(newStatusEvent), null);
            statusRemovedActivities.add(activity);
        }
        return statusRemovedActivities;
    }

     private QuestionableActivityDeveloper getQuestionableActivityDeveloper(String before, String after) {
         QuestionableActivityDeveloper activity = new QuestionableActivityDeveloper();
         activity.setBefore(before);
         activity.setAfter(after);
         return activity;
     }

    private String getFormattedStatus(DeveloperStatusEvent statusEvent) {
        return statusEvent.getStatus().getName()
                + " (" + Util.getDateFormatter().format(statusEvent.getStartDay())
                + (statusEvent.getEndDay() == null ? " - ? " : " - " + Util.getDateFormatter().format(statusEvent.getEndDay()))
                + ")";
    }
}
