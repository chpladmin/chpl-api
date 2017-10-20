package gov.healthit.chpl.quesitonableActivity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CQMResultDetails;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityCertificationResultDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityDeveloperDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;

@Component
public class ProductQuestionableActivityProvider {
    
    public QuestionableActivityProductDTO checkNameUpdated(ProductDTO origProduct, ProductDTO newProduct) {
        
        QuestionableActivityProductDTO activity = null;
        if ((origProduct.getName() != null && newProduct.getName() == null)
                || (origProduct.getName() == null && newProduct.getName() != null)
                || !origProduct.getName().equals(newProduct.getName())) {
            activity = new QuestionableActivityProductDTO();
            activity.setMessage("From " + origProduct.getName() + " to " + newProduct.getName());
        }
        
        return activity;
    }
    
    public QuestionableActivityProductDTO checkCurrentOwnerChanged(
            ProductDTO origProduct, ProductDTO newProduct) {
        
        QuestionableActivityProductDTO activity = null;
        //TODO
        
        return activity;
    }
    
    public List<QuestionableActivityProductDTO> checkOwnerHistoryChanged(
            ProductDTO origProduct, ProductDTO newProduct) {
        
        List<QuestionableActivityProductDTO> activities = null;
        //TODO
     // if there was a different amount of owner history
//        if ((original.getOwnerHistory() != null && changed.getOwnerHistory() == null)
//                || (original.getOwnerHistory() == null && changed.getOwnerHistory() != null)
//                || (original.getOwnerHistory().size() != changed.getOwnerHistory().size())) {
//            isQuestionable = true;
//        } else {
//            // the same counts of owner history are there but we should
//            // check the contents
//            for (ProductOwnerDTO originalOwner : original.getOwnerHistory()) {
//                boolean foundOriginalOwner = false;
//                for (ProductOwnerDTO changedOwner : changed.getOwnerHistory()) {
//                    if (originalOwner.getDeveloper().getId().longValue() == changedOwner.getDeveloper().getId()
//                            .longValue()) {
//                        foundOriginalOwner = true;
//                    }
//                }
//                if (!foundOriginalOwner) {
//                    isQuestionable = true;
//                }
//            }
//        }
        return activities;
    }
}
