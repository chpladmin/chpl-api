package gov.healthit.chpl.questionableactivity.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.ProductOwner;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityProductDTO;

/**
 * Checks for Product questionable activities.
 */
@Component
public class ProductQuestionableActivityProvider {

    /**
     * Check for QA re: product name.
     * @param origProduct original product
     * @param newProduct new product
     * @return DTO of questionable activity
     */
    public QuestionableActivityProductDTO checkNameUpdated(Product origProduct, Product newProduct) {
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

    /**
     * Check for QA re: product current owner.
     * @param origProduct original product
     * @param newProduct new product
     * @return DTO of questionable activity
     */
    public QuestionableActivityProductDTO checkCurrentOwnerChanged(Product origProduct, Product newProduct) {
        QuestionableActivityProductDTO activity = null;
        Developer origOwner = origProduct.getOwner();
        Developer newOwner = newProduct.getOwner();
        if (origOwner != null && origOwner.getId() != null
                && (newOwner == null || newOwner.getId() == null)) {
            activity = new QuestionableActivityProductDTO();
            activity.setBefore(origOwner.getName());
            activity.setAfter(null);
        } else if ((origOwner == null || origOwner.getId() == null)
                && newOwner != null && newOwner.getId() != null) {
            activity = new QuestionableActivityProductDTO();
            activity.setBefore(null);
            activity.setAfter(newOwner.getName());
        } else if (origOwner != null && newOwner != null
                && origOwner.getId().longValue() != newOwner.getId().longValue()) {
            activity = new QuestionableActivityProductDTO();
            activity.setBefore(origOwner.getName());
            activity.setAfter(newOwner.getName());
        }
        return activity;
    }

    /**
     * Check for QA re: product owner history.
     * @param origOwners original product owners
     * @param newOwners new product owners
     * @return list of added owners
     */
    public List<QuestionableActivityProductDTO> checkOwnerHistoryAdded(
            List<ProductOwner> origOwners, List<ProductOwner> newOwners) {

        List<QuestionableActivityProductDTO> ownerAddedActivities = new ArrayList<QuestionableActivityProductDTO>();
        if ((origOwners == null || origOwners.size() == 0)
                && newOwners != null && newOwners.size() > 0) {
            //all the newOwners are "added"
            for (ProductOwner newOwner : newOwners) {
                QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                activity.setBefore(null);
                activity.setAfter(newOwner.getDeveloper().getName() + " (" + newOwner.getTransferDay() + ")");
                ownerAddedActivities.add(activity);
            }
        } else if (origOwners != null && origOwners.size() > 0
                && newOwners != null && newOwners.size() > 0) {
            for (ProductOwner newOwner : newOwners) {
                boolean foundOwner = false;
                for (ProductOwner origOwner : origOwners) {
                    if (origOwner.getId().equals(newOwner.getId())
                            || (origOwner.getDeveloper().getId().equals(newOwner.getDeveloper().getId())
                                    && origOwner.getTransferDay().equals(newOwner.getTransferDay()))) {
                        foundOwner = true;
                    }
                }
                //new owner had this item but old did not
                if (!foundOwner) {
                    QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                    activity.setBefore(null);
                    activity.setAfter(newOwner.getDeveloper().getName() + " (" + newOwner.getTransferDay() + ")");
                    ownerAddedActivities.add(activity);
                }
            }
        }
        return ownerAddedActivities;
    }

    /**
     * Check for QA re: product owner history.
     * @param origOwners original product owners
     * @param newOwners new product owners
     * @return list of removed owners
     */
    public List<QuestionableActivityProductDTO> checkOwnerHistoryRemoved(
            List<ProductOwner> origOwners, List<ProductOwner> newOwners) {

        List<QuestionableActivityProductDTO> ownerRemovedActivities = new ArrayList<QuestionableActivityProductDTO>();
        if (origOwners != null && origOwners.size() > 0
                && (newOwners == null || newOwners.size() == 0)) {
            //all the origOwners are "removed"
            for (ProductOwner origOwner : origOwners) {
                QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                activity.setBefore(origOwner.getDeveloper().getName() + " (" + origOwner.getTransferDay() + ")");
                activity.setAfter(null);
                ownerRemovedActivities.add(activity);
            }
        } else if (origOwners != null && origOwners.size() > 0
                && newOwners != null && newOwners.size() > 0) {
            for (ProductOwner origOwner : origOwners) {
                boolean foundOwner = false;
                for (ProductOwner newOwner : newOwners) {
                    if (origOwner.getId().equals(newOwner.getId())
                            || (origOwner.getDeveloper().getId().equals(newOwner.getDeveloper().getId())
                                    && origOwner.getTransferDay().equals(newOwner.getTransferDay()))) {
                        foundOwner = true;
                    }
                }
                //orig owner had this item but new did not
                if (!foundOwner) {
                    QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                    activity.setBefore(origOwner.getDeveloper().getName() + " (" + origOwner.getTransferDay() + ")");
                    activity.setAfter(null);
                    ownerRemovedActivities.add(activity);
                }
            }
        }
        return ownerRemovedActivities;
    }

    /**
     * Check for QA re: product owner history.
     * @param origOwners original product owners
     * @param newOwners new product owners
     * @return list of edited owners
     */
    public List<QuestionableActivityProductDTO> checkOwnerHistoryItemEdited(
            List<ProductOwner> origOwners, List<ProductOwner> newOwners) {

        List<QuestionableActivityProductDTO> ownerEditedActivities = new ArrayList<QuestionableActivityProductDTO>();
        if (origOwners != null && origOwners.size() > 0
                && newOwners != null && newOwners.size() > 0) {
            for (ProductOwner origOwner : origOwners) {
                boolean ownerEdited = false;
                ProductOwner matchingNewOwner = null;
                for (ProductOwner newOwner : newOwners) {
                    if (origOwner.getId().equals(newOwner.getId())) {
                        matchingNewOwner = newOwner;
                        //same id, check if the owner name and date are still the same
                        if (!origOwner.getTransferDay().equals(newOwner.getTransferDay())) {
                            ownerEdited = true;
                        } else if (origOwner.getDeveloper().getId().longValue()
                                != newOwner.getDeveloper().getId().longValue()) {
                            ownerEdited = true;
                        }
                    }
                }
                //orig owner history item was edited
                if (ownerEdited) {
                    QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                    activity.setBefore(origOwner.getDeveloper().getName() + " (" + origOwner.getTransferDay() + ")");
                    activity.setAfter(matchingNewOwner.getDeveloper().getName() + " (" + matchingNewOwner.getTransferDay() + ")");
                    ownerEditedActivities.add(activity);
                }
            }
        }
        return ownerEditedActivities;
    }
}
