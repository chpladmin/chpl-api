package gov.healthit.chpl.quesitonableActivity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;

@Component
public class DeveloperQuestionableActivityProvider {
    
    public QuestionableActivityDeveloperDTO checkNameUpdated(DeveloperDTO origDeveloper, DeveloperDTO newDeveloper) {
        
        QuestionableActivityDeveloperDTO activity = null;
        if ((origDeveloper.getName() != null && newDeveloper.getName() == null)
                || (origDeveloper.getName() == null && newDeveloper.getName() != null)
                || !origDeveloper.getName().equals(newDeveloper.getName())) {
            activity = new QuestionableActivityDeveloperDTO();
            activity.setMessage("From " + origDeveloper.getName() + " to " + newDeveloper.getName());
        }
        
        return activity;
    }
    
    public QuestionableActivityDeveloperDTO checkCurrentStatusChanged(
            DeveloperDTO origDeveloper, DeveloperDTO newDeveloper) {
        
        QuestionableActivityDeveloperDTO activity = null;
        //TODO
        
        return activity;
    }
    
    public List<QuestionableActivityDeveloperDTO> checkStatusHistoryChanged(
            DeveloperDTO origDeveloper, DeveloperDTO newDeveloper) {
        
        List<QuestionableActivityDeveloperDTO> activities = null;
        //TODO
//        if ((original.getStatusEvents() != null && changed.getStatusEvents() == null)
//                || (original.getStatusEvents() == null && changed.getStatusEvents() != null)
//                || (original.getStatusEvents().size() != changed.getStatusEvents().size())) {
//            hasChanged = true;
//        } else {
//            // neither status history is null and they have the same size
//            // history arrays
//            // so now check for any differences in the values of each
//            for (DeveloperStatusEventDTO origStatusHistory : original.getStatusEvents()) {
//                boolean foundMatchInChanged = false;
//                for (DeveloperStatusEventDTO changedStatusHistory : changed.getStatusEvents()) {
//                    if (origStatusHistory.getStatus().getId().longValue() == changedStatusHistory.getStatus().getId()
//                            .longValue()
//                            && origStatusHistory.getStatusDate().getTime() == changedStatusHistory.getStatusDate()
//                                    .getTime()) {
//                        foundMatchInChanged = true;
//                    }
//                }
//                hasChanged = hasChanged || !foundMatchInChanged;
//            }
//        }
        return activities;
    }
}
