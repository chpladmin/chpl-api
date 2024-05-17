package gov.healthit.chpl.questionableactivity.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatusEventDeprecated;
import gov.healthit.chpl.dto.DeveloperStatusEventPair;
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
        if (origDeveloper.getStatus() != null && newDeveloper.getStatus() == null) {
            activity = new QuestionableActivityDeveloper();
            activity.setBefore(origDeveloper.getStatus().getStatus());
            activity.setAfter(null);
        } else if (origDeveloper.getStatus() == null && newDeveloper.getStatus() != null) {
            activity = new QuestionableActivityDeveloper();
            activity.setBefore(null);
            activity.setAfter(newDeveloper.getStatus().getStatus());
        } else if (origDeveloper.getStatus().getId().longValue()
                != newDeveloper.getStatus().getId().longValue()) {
            activity = new QuestionableActivityDeveloper();
            activity.setBefore(origDeveloper.getStatus().getStatus());
            activity.setAfter(newDeveloper.getStatus().getStatus());
        }

        return activity;
    }

    /**
     * Check for QA re: developer status history.
     * @param origStatuses original developer status events
     * @param newStatuses new developer status events
     * @return list of added statuses
     */
     public List<QuestionableActivityDeveloper> checkStatusHistoryAdded(
            List<DeveloperStatusEventDeprecated> origStatuses, List<DeveloperStatusEventDeprecated> newStatuses) {

         List<QuestionableActivityDeveloper> statusAddedActivities
             = new ArrayList<QuestionableActivityDeveloper>();

         List<DeveloperStatusEventDeprecated> addedStatuses =
                 DeveloperStatusEventsHelper.getAddedEvents(origStatuses, newStatuses);
         for (DeveloperStatusEventDeprecated newStatusEvent : addedStatuses) {
             QuestionableActivityDeveloper activity =
                     getQuestionableActivityDeveloper(null, getFormattedStatus(newStatusEvent));
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
            List<DeveloperStatusEventDeprecated> origStatuses, List<DeveloperStatusEventDeprecated> newStatuses) {

        List<QuestionableActivityDeveloper> statusRemovedActivities
            = new ArrayList<QuestionableActivityDeveloper>();

        List<DeveloperStatusEventDeprecated> removedStatuses =
                DeveloperStatusEventsHelper.getRemovedEvents(origStatuses, newStatuses);
        for (DeveloperStatusEventDeprecated newStatusEvent : removedStatuses) {
            QuestionableActivityDeveloper activity =
                    getQuestionableActivityDeveloper(getFormattedStatus(newStatusEvent), null);
            statusRemovedActivities.add(activity);
        }
        return statusRemovedActivities;
    }

    /**
     * Check for QA re: developer status history.
     * @param origStatuses original developer status events
     * @param newStatuses new developer status events
     * @return list of edited statuses
     */
    public List<QuestionableActivityDeveloper> checkStatusHistoryItemEdited(
            List<DeveloperStatusEventDeprecated> origStatuses, List<DeveloperStatusEventDeprecated> newStatuses) {

        List<QuestionableActivityDeveloper> statusEditedActivities
            = new ArrayList<QuestionableActivityDeveloper>();
        List<DeveloperStatusEventPair> eventPairs =
                DeveloperStatusEventsHelper.getUpdatedEvents(origStatuses, newStatuses);
        for (DeveloperStatusEventPair eventPair : eventPairs) {
            QuestionableActivityDeveloper activity =
                    getQuestionableActivityDeveloper(
                            getFormattedStatus(eventPair.getOrig()),
                            getFormattedStatus(eventPair.getUpdated()));
            activity.setReason(eventPair.getUpdated().getReason());
            statusEditedActivities.add(activity);
        }
        return statusEditedActivities;
    }

    private QuestionableActivityDeveloper getQuestionableActivityDeveloper(String before, String after) {
        QuestionableActivityDeveloper activity = new QuestionableActivityDeveloper();
        activity.setBefore(before);
        activity.setAfter(after);
        return activity;
    }

    private String getFormattedStatus(DeveloperStatusEventDeprecated statusEvent) {
        return statusEvent.getStatus().getStatus() + " ("
                + Util.getDateFormatter().format(statusEvent.getStatusDate()) + ")";
    }
}
