package gov.healthit.chpl.quesitonableActivity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;

@Component
public class DeveloperQuestionableActivityProvider {
    
    public QuestionableActivityDeveloperDTO checkNameUpdated(DeveloperDTO origDeveloper, DeveloperDTO newDeveloper) {
        
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
    
    public QuestionableActivityDeveloperDTO checkCurrentStatusChanged(
            DeveloperDTO origDeveloper, DeveloperDTO newDeveloper) {
        
        QuestionableActivityDeveloperDTO activity = null;
        if(origDeveloper.getStatus() != null && newDeveloper.getStatus() == null) {
            activity = new QuestionableActivityDeveloperDTO();
            activity.setBefore(origDeveloper.getStatus().getStatus().getStatusName());
            activity.setAfter(null);
        } else if(origDeveloper.getStatus() == null && newDeveloper.getStatus() != null) {
            activity = new QuestionableActivityDeveloperDTO();
            activity.setBefore(null);
            activity.setAfter(newDeveloper.getStatus().getStatus().getStatusName());
        } else if(origDeveloper.getStatus().getStatus().getId().longValue() != 
                newDeveloper.getStatus().getStatus().getId().longValue()) {
            activity = new QuestionableActivityDeveloperDTO();
            activity.setBefore(origDeveloper.getStatus().getStatus().getStatusName());
            activity.setAfter(newDeveloper.getStatus().getStatus().getStatusName());
        }
        
        return activity;
    }
    
    public List<QuestionableActivityDeveloperDTO> checkStatusHistoryAdded(
            List<DeveloperStatusEventDTO> origStatuses, List<DeveloperStatusEventDTO> newStatuses) {
        
        List<QuestionableActivityDeveloperDTO> statusAddedActivities = new ArrayList<QuestionableActivityDeveloperDTO>();        
        if ((origStatuses == null || origStatuses.size() == 0) && 
                newStatuses != null && newStatuses.size() > 0) {
            //all the new status events are "added"
            for(DeveloperStatusEventDTO newStatusEvent : newStatuses) {
                QuestionableActivityDeveloperDTO activity = new QuestionableActivityDeveloperDTO();
                activity.setBefore(null);
                activity.setAfter(newStatusEvent.getStatus().getStatusName() + " (" + newStatusEvent.getStatusDate() + ")");
                statusAddedActivities.add(activity);
            }
        } else if (origStatuses != null && origStatuses.size() > 0 && 
                newStatuses != null && newStatuses.size() > 0) {
            for (DeveloperStatusEventDTO newStatusEvent : newStatuses) {
                boolean foundStatus = false;
                for (DeveloperStatusEventDTO origStatusEvent : origStatuses) {
                    if (origStatusEvent.getId().equals(newStatusEvent.getId()) || 
                         (origStatusEvent.getStatus().getId().longValue() == newStatusEvent.getStatus().getId().longValue() && 
                         origStatusEvent.getStatusDate().getTime() == newStatusEvent.getStatusDate().getTime())) {
                        foundStatus = true;
                    }
                }
                //new dev status history had this item but old did not
                if(!foundStatus) {
                    QuestionableActivityDeveloperDTO activity = new QuestionableActivityDeveloperDTO();
                    activity.setBefore(null);
                    activity.setAfter(newStatusEvent.getStatus().getStatusName() + " (" + newStatusEvent.getStatusDate() + ")");                    statusAddedActivities.add(activity);
                }
            }
        }
        return statusAddedActivities;
    }
    
    public List<QuestionableActivityDeveloperDTO> checkStatusHistoryRemoved(
            List<DeveloperStatusEventDTO> origStatuses, List<DeveloperStatusEventDTO> newStatuses) {
        
        List<QuestionableActivityDeveloperDTO> statusAddedActivities = new ArrayList<QuestionableActivityDeveloperDTO>();        
        if ((origStatuses != null && origStatuses.size() > 0) && 
                newStatuses == null || newStatuses.size() == 0) {
            //all the orig status events are "removed"
            for(DeveloperStatusEventDTO origStatusEvent : origStatuses) {
                QuestionableActivityDeveloperDTO activity = new QuestionableActivityDeveloperDTO();
                activity.setBefore(origStatusEvent.getStatus().getStatusName() + " (" + origStatusEvent.getStatusDate() + ")");
                activity.setAfter(null);
                statusAddedActivities.add(activity);
            }
        } else if (origStatuses != null && origStatuses.size() > 0 && 
                newStatuses != null && newStatuses.size() > 0) {
            for (DeveloperStatusEventDTO origStatusEvent : origStatuses) {
                boolean foundStatus = false;
                for (DeveloperStatusEventDTO newStatusEvent : newStatuses) {
                    if (origStatusEvent.getId().equals(newStatusEvent.getId()) || 
                         (origStatusEvent.getStatus().getId().longValue() == newStatusEvent.getStatus().getId().longValue() && 
                          origStatusEvent.getStatusDate().getTime() == newStatusEvent.getStatusDate().getTime())) {
                        foundStatus = true;
                    }
                }
                //orig dev status history had this item but new did not
                if(!foundStatus) {
                    QuestionableActivityDeveloperDTO activity = new QuestionableActivityDeveloperDTO();
                    activity.setBefore(origStatusEvent.getStatus().getStatusName() + " (" + origStatusEvent.getStatusDate() + ")");
                    activity.setAfter(null);
                    statusAddedActivities.add(activity);
                }
            }
        }
        return statusAddedActivities;
    }
    
    public List<QuestionableActivityDeveloperDTO> checkStatusHistoryItemEdited(
            List<DeveloperStatusEventDTO> origStatuses, List<DeveloperStatusEventDTO> newStatuses) {
        
        List<QuestionableActivityDeveloperDTO> statusEditedActivities = new ArrayList<QuestionableActivityDeveloperDTO>();        
        if (origStatuses != null && origStatuses.size() > 0 && 
                newStatuses != null && newStatuses.size() > 0) {
            for (DeveloperStatusEventDTO origStatusEvent : origStatuses) {
                boolean statusEdited = false;
                DeveloperStatusEventDTO matchingNewStatusEvent = null;
                for (DeveloperStatusEventDTO newStatusEvent : newStatuses) {
                    if (origStatusEvent.getId().equals(newStatusEvent.getId())) {
                        matchingNewStatusEvent = newStatusEvent;
                        //same status id, check if the status name and date are still the same
                        if(origStatusEvent.getStatusDate().getTime() != newStatusEvent.getStatusDate().getTime()) {
                            statusEdited = true;
                        } else if(origStatusEvent.getStatus().getId().longValue() != 
                                newStatusEvent.getStatus().getId().longValue()) {
                            statusEdited = true;
                        }
                    }
                }
                //orig dev status history item was edited
                if(statusEdited) {
                    QuestionableActivityDeveloperDTO activity = new QuestionableActivityDeveloperDTO();
                    activity.setBefore(origStatusEvent.getStatus().getStatusName() + " (" + origStatusEvent.getStatusDate() + ")");
                    activity.setAfter(matchingNewStatusEvent.getStatus().getStatusName() + " (" + matchingNewStatusEvent.getStatusDate() + ")");
                    statusEditedActivities.add(activity);
                }
            }
        }
        return statusEditedActivities;
    }
}
