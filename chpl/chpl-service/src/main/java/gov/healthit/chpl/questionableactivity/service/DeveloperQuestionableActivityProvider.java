package gov.healthit.chpl.questionableactivity.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.dto.DeveloperStatusEventPair;
import gov.healthit.chpl.manager.impl.DeveloperStatusEventsHelper;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.util.Util;

@Component
public class DeveloperQuestionableActivityProvider {

    /**
     * Check for QA re: developer name.
     * @param origDeveloper original developer
     * @param newDeveloper new developer
     * @return DTO if developer name changed
     */
     public QuestionableActivityDeveloperDTO checkNameUpdated(Developer origDeveloper, Developer newDeveloper) {

        QuestionableActivityDeveloperDTO activity = null;
        if ((origDeveloper.getName() != null && newDeveloper.getName() == null)
                || (origDeveloper.getName() == null && newDeveloper.getName() != null)
                || !origDeveloper.getName().equals(newDeveloper.getName())) {
            activity = new QuestionableActivityDeveloperDTO();
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
    public QuestionableActivityDeveloperDTO checkCurrentStatusChanged(Developer origDeveloper, Developer newDeveloper) {

        QuestionableActivityDeveloperDTO activity = null;
        if (origDeveloper.getStatus() != null && newDeveloper.getStatus() == null) {
            activity = new QuestionableActivityDeveloperDTO();
            activity.setBefore(origDeveloper.getStatus().getStatus());
            activity.setAfter(null);
        } else if (origDeveloper.getStatus() == null && newDeveloper.getStatus() != null) {
            activity = new QuestionableActivityDeveloperDTO();
            activity.setBefore(null);
            activity.setAfter(newDeveloper.getStatus().getStatus());
        } else if (origDeveloper.getStatus().getId().longValue()
                != newDeveloper.getStatus().getId().longValue()) {
            activity = new QuestionableActivityDeveloperDTO();
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
     public List<QuestionableActivityDeveloperDTO> checkStatusHistoryAdded(
            List<DeveloperStatusEvent> origStatuses, List<DeveloperStatusEvent> newStatuses) {

         List<QuestionableActivityDeveloperDTO> statusAddedActivities
             = new ArrayList<QuestionableActivityDeveloperDTO>();

         List<DeveloperStatusEvent> addedStatuses =
                 DeveloperStatusEventsHelper.getAddedEvents(origStatuses, newStatuses);
         for (DeveloperStatusEvent newStatusEvent : addedStatuses) {
             QuestionableActivityDeveloperDTO activity =
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
     public List<QuestionableActivityDeveloperDTO> checkStatusHistoryRemoved(
            List<DeveloperStatusEvent> origStatuses, List<DeveloperStatusEvent> newStatuses) {

        List<QuestionableActivityDeveloperDTO> statusRemovedActivities
            = new ArrayList<QuestionableActivityDeveloperDTO>();

        List<DeveloperStatusEvent> removedStatuses =
                DeveloperStatusEventsHelper.getRemovedEvents(origStatuses, newStatuses);
        for (DeveloperStatusEvent newStatusEvent : removedStatuses) {
            QuestionableActivityDeveloperDTO activity =
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
    public List<QuestionableActivityDeveloperDTO> checkStatusHistoryItemEdited(
            List<DeveloperStatusEvent> origStatuses, List<DeveloperStatusEvent> newStatuses) {

        List<QuestionableActivityDeveloperDTO> statusEditedActivities
            = new ArrayList<QuestionableActivityDeveloperDTO>();
        List<DeveloperStatusEventPair> eventPairs =
                DeveloperStatusEventsHelper.getUpdatedEvents(origStatuses, newStatuses);
        for (DeveloperStatusEventPair eventPair : eventPairs) {
            QuestionableActivityDeveloperDTO activity =
                    getQuestionableActivityDeveloper(
                            getFormattedStatus(eventPair.getOrig()),
                            getFormattedStatus(eventPair.getUpdated()));
            activity.setReason(eventPair.getUpdated().getReason());
            statusEditedActivities.add(activity);
        }
        return statusEditedActivities;
    }

    private QuestionableActivityDeveloperDTO getQuestionableActivityDeveloper(String before, String after) {
        QuestionableActivityDeveloperDTO activity = new QuestionableActivityDeveloperDTO();
        activity.setBefore(before);
        activity.setAfter(after);
        return activity;
    }

    private String getFormattedStatus(DeveloperStatusEvent statusEvent) {
        return statusEvent.getStatus().getStatus() + " ("
                + Util.getDateFormatter().format(statusEvent.getStatusDate()) + ")";
    }
}
