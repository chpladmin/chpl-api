package gov.healthit.chpl.questionableActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.Util;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;

@Component
public class ProductQuestionableActivityProvider {
    
    public QuestionableActivityProductDTO checkNameUpdated(ProductDTO origProduct, ProductDTO newProduct) {
        
        QuestionableActivityProductDTO activity = null;
        if ((origProduct.getName() != null && newProduct.getName() == null)
                || (origProduct.getName() == null && newProduct.getName() != null)
                || !origProduct.getName().equals(newProduct.getName())) {
            activity = new QuestionableActivityProductDTO();
            activity.setBefore(origProduct.getName());
            activity.setAfter(newProduct.getName());
        }
        
        return activity;
    }
    
    public QuestionableActivityProductDTO checkCurrentOwnerChanged(
            ProductDTO origProduct, ProductDTO newProduct) {
        
        QuestionableActivityProductDTO activity = null;
        if(origProduct.getDeveloperId() != null && newProduct.getDeveloperId() == null) {
            activity = new QuestionableActivityProductDTO();
            activity.setBefore(origProduct.getDeveloperName());
            activity.setAfter(null);
        } else if(origProduct.getDeveloperId() == null && newProduct.getDeveloperId() != null) {
            activity = new QuestionableActivityProductDTO();
            activity.setBefore(null);
            activity.setAfter(newProduct.getDeveloperName());
        } else if(origProduct.getDeveloperId().longValue() != newProduct.getDeveloperId().longValue()) {
            activity = new QuestionableActivityProductDTO();
            activity.setBefore(origProduct.getDeveloperName());
            activity.setAfter(newProduct.getDeveloperName());
        }
        return activity;
    }
    
    public List<QuestionableActivityProductDTO> checkOwnerHistoryAdded(
            List<ProductOwnerDTO> origOwners, List<ProductOwnerDTO> newOwners) {
        
        List<QuestionableActivityProductDTO> ownerAddedActivities = new ArrayList<QuestionableActivityProductDTO>();        
        if ((origOwners == null || origOwners.size() == 0) && 
                newOwners != null && newOwners.size() > 0) {
            //all the newOwners are "added"
            for(ProductOwnerDTO newOwner : newOwners) {
                QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                activity.setBefore(null);
                activity.setAfter(newOwner.getDeveloper().getName() + 
                        " (" + Util.getDateFormatter().format(new Date(newOwner.getTransferDate())) + ")");
                ownerAddedActivities.add(activity);
            }
        } else if (origOwners != null && origOwners.size() > 0 && 
                newOwners != null && newOwners.size() > 0) {
            for (ProductOwnerDTO newOwner : newOwners) {
                boolean foundOwner = false;
                for (ProductOwnerDTO origOwner : origOwners) {
                    if (origOwner.getId().equals(newOwner.getId()) || 
                         (origOwner.getDeveloper().getId().longValue() == newOwner.getDeveloper().getId().longValue() && 
                         origOwner.getTransferDate().longValue() == newOwner.getTransferDate().longValue())) {
                        foundOwner = true;
                    }
                }
                //new owner had this item but old did not
                if(!foundOwner) {
                    QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                    activity.setBefore(null);
                    activity.setAfter(newOwner.getDeveloper().getName() + 
                            " (" + Util.getDateFormatter().format(new Date(newOwner.getTransferDate())) + ")");
                    ownerAddedActivities.add(activity);
                }
            }
        }
        return ownerAddedActivities;
    }
    
    public List<QuestionableActivityProductDTO> checkOwnerHistoryRemoved(
            List<ProductOwnerDTO> origOwners, List<ProductOwnerDTO> newOwners) {
        
        List<QuestionableActivityProductDTO> ownerRemovedActivities = new ArrayList<QuestionableActivityProductDTO>();        
        if (origOwners != null && origOwners.size() > 0 && 
                (newOwners == null || newOwners.size() == 0)) {
            //all the origOwners are "removed"
            for(ProductOwnerDTO origOwner : origOwners) {
                QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                activity.setBefore(origOwner.getDeveloper().getName() + 
                        " (" + Util.getDateFormatter().format(new Date(origOwner.getTransferDate())) + ")");
                activity.setAfter(null);
                ownerRemovedActivities.add(activity);
            }
        } else if (origOwners != null && origOwners.size() > 0 && 
                newOwners != null && newOwners.size() > 0) {
            for (ProductOwnerDTO origOwner : origOwners) {
                boolean foundOwner = false;
                for (ProductOwnerDTO newOwner : newOwners) {
                    if (origOwner.getId().equals(newOwner.getId()) || 
                         (origOwner.getDeveloper().getId().longValue() == newOwner.getDeveloper().getId().longValue() && 
                         origOwner.getTransferDate().longValue() == newOwner.getTransferDate().longValue())) {
                        foundOwner = true;
                    }
                }
                //orig owner had this item but new did not
                if(!foundOwner) {
                    QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                    activity.setBefore(origOwner.getDeveloper().getName() + 
                            " (" + Util.getDateFormatter().format(new Date(origOwner.getTransferDate())) + ")");
                    activity.setAfter(null);
                    ownerRemovedActivities.add(activity);
                }
            }
        }
        return ownerRemovedActivities;
    }
    
    public List<QuestionableActivityProductDTO> checkOwnerHistoryItemEdited(
            List<ProductOwnerDTO> origOwners, List<ProductOwnerDTO> newOwners) {
        
        List<QuestionableActivityProductDTO> ownerEditedActivities = new ArrayList<QuestionableActivityProductDTO>();        
        if (origOwners != null && origOwners.size() > 0 && 
                newOwners != null && newOwners.size() > 0) {
            for (ProductOwnerDTO origOwner : origOwners) {
                boolean ownerEdited = false;
                ProductOwnerDTO matchingNewOwner = null;
                for (ProductOwnerDTO newOwner : newOwners) {
                    if (origOwner.getId().equals(newOwner.getId())) {
                        matchingNewOwner = newOwner;
                        //same id, check if the owner name and date are still the same
                        if(origOwner.getTransferDate().longValue() != newOwner.getTransferDate().longValue()) {
                            ownerEdited = true;
                        } else if(origOwner.getDeveloper().getId().longValue() != 
                                newOwner.getDeveloper().getId().longValue()) {
                            ownerEdited = true;
                        }
                    }
                }
                //orig owner history item was edited
                if(ownerEdited) {
                    QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                    activity.setBefore(origOwner.getDeveloper().getName() + 
                            " (" + Util.getDateFormatter().format(new Date(origOwner.getTransferDate())) + ")");
                    activity.setAfter(matchingNewOwner.getDeveloper().getName() + 
                            " (" + Util.getDateFormatter().format(new Date(matchingNewOwner.getTransferDate())) + ")");
                    ownerEditedActivities.add(activity);
                }
            }
        }
        return ownerEditedActivities;
    }
}
