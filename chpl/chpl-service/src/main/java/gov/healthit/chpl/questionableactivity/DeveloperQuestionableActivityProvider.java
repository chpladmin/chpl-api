package gov.healthit.chpl.questionableactivity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventPair;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.manager.impl.DeveloperStatusEventsHelper;
import gov.healthit.chpl.util.Util;

/**
 * Check for questionable activity related to a Developer.
 */
@Component
public class DeveloperQuestionableActivityProvider {

    /**
     * Check for QA re: developer name.
     * @param origDeveloper original developer
     * @param newDeveloper new developer
     * @return DTO if developer name changed
     */
     public QuestionableActivityDeveloperDTO checkNameUpdated(
            final DeveloperDTO origDeveloper, final DeveloperDTO newDeveloper) {

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
    public QuestionableActivityDeveloperDTO checkCurrentStatusChanged(
            final DeveloperDTO origDeveloper, final DeveloperDTO newDeveloper) {

        QuestionableActivityDeveloperDTO activity = null;
        if (origDeveloper.getStatus() != null && newDeveloper.getStatus() == null) {
            activity = new QuestionableActivityDeveloperDTO();
            activity.setBefore(origDeveloper.getStatus().getStatus().getStatusName());
            activity.setAfter(null);
        } else if (origDeveloper.getStatus() == null && newDeveloper.getStatus() != null) {
            activity = new QuestionableActivityDeveloperDTO();
            activity.setBefore(null);
            activity.setAfter(newDeveloper.getStatus().getStatus().getStatusName());
        } else if (origDeveloper.getStatus().getStatus().getId().longValue()
                != newDeveloper.getStatus().getStatus().getId().longValue()) {
            activity = new QuestionableActivityDeveloperDTO();
            activity.setBefore(origDeveloper.getStatus().getStatus().getStatusName());
            activity.setAfter(newDeveloper.getStatus().getStatus().getStatusName());
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
            final List<DeveloperStatusEventDTO> origStatuses, final List<DeveloperStatusEventDTO> newStatuses) {

         List<QuestionableActivityDeveloperDTO> statusAddedActivities
             = new ArrayList<QuestionableActivityDeveloperDTO>();

         List<DeveloperStatusEventDTO> addedStatuses =
                 DeveloperStatusEventsHelper.getAddedEvents(origStatuses, newStatuses);
         for (DeveloperStatusEventDTO newStatusEvent : addedStatuses) {
             QuestionableActivityDeveloperDTO activity =
                     getQuestionableActivityDeveloper(null, getFormattedStatus(newStatusEvent));
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
            final List<DeveloperStatusEventDTO> origStatuses, final List<DeveloperStatusEventDTO> newStatuses) {

        List<QuestionableActivityDeveloperDTO> statusRemovedActivities
            = new ArrayList<QuestionableActivityDeveloperDTO>();

        List<DeveloperStatusEventDTO> removedStatuses =
                DeveloperStatusEventsHelper.getRemovedEvents(origStatuses, newStatuses);
        for (DeveloperStatusEventDTO newStatusEvent : removedStatuses) {
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
            final List<DeveloperStatusEventDTO> origStatuses, final List<DeveloperStatusEventDTO> newStatuses) {

        List<QuestionableActivityDeveloperDTO> statusEditedActivities
            = new ArrayList<QuestionableActivityDeveloperDTO>();
        List<DeveloperStatusEventPair> eventPairs =
                DeveloperStatusEventsHelper.getUpdatedEvents(origStatuses, newStatuses);
        for (DeveloperStatusEventPair eventPair : eventPairs) {
            QuestionableActivityDeveloperDTO activity =
                    getQuestionableActivityDeveloper(
                            getFormattedStatus(eventPair.getOrig()),
                            getFormattedStatus(eventPair.getUpdated()));
            statusEditedActivities.add(activity);
        }
        return statusEditedActivities;
    }

    private QuestionableActivityDeveloperDTO getQuestionableActivityDeveloper(final String before,
            final String after) {
        QuestionableActivityDeveloperDTO activity = new QuestionableActivityDeveloperDTO();
        activity.setBefore(before);
        activity.setAfter(after);
        return activity;
    }

    private String getFormattedStatus(final DeveloperStatusEventDTO statusEvent) {
        return statusEvent.getStatus().getStatusName() + " ("
                + Util.getDateFormatter().format(statusEvent.getStatusDate()) + ")";
    }
}
