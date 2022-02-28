package gov.healthit.chpl.questionableactivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.dto.ProductDTO;
import gov.healthit.chpl.dto.ProductOwnerDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityProductDTO;
import gov.healthit.chpl.util.Util;

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
    public QuestionableActivityProductDTO checkNameUpdated(final ProductDTO origProduct, final ProductDTO newProduct) {
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
    public QuestionableActivityProductDTO checkCurrentOwnerChanged(
            ProductDTO origProduct, ProductDTO newProduct) {

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
            final List<ProductOwnerDTO> origOwners, final List<ProductOwnerDTO> newOwners) {

        List<QuestionableActivityProductDTO> ownerAddedActivities = new ArrayList<QuestionableActivityProductDTO>();
        if ((origOwners == null || origOwners.size() == 0)
                && newOwners != null && newOwners.size() > 0) {
            //all the newOwners are "added"
            for (ProductOwnerDTO newOwner : newOwners) {
                QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                activity.setBefore(null);
                activity.setAfter(newOwner.getDeveloper().getName()
                        + " (" + Util.getDateFormatter().format(new Date(newOwner.getTransferDate())) + ")");
                ownerAddedActivities.add(activity);
            }
        } else if (origOwners != null && origOwners.size() > 0
                && newOwners != null && newOwners.size() > 0) {
            for (ProductOwnerDTO newOwner : newOwners) {
                boolean foundOwner = false;
                for (ProductOwnerDTO origOwner : origOwners) {
                    if (origOwner.getId().equals(newOwner.getId())
                            || (origOwner.getDeveloper().getId().longValue()
                                    == newOwner.getDeveloper().getId().longValue()
                                    && origOwner.getTransferDate().longValue() == newOwner.getTransferDate().longValue())) {
                        foundOwner = true;
                    }
                }
                //new owner had this item but old did not
                if (!foundOwner) {
                    QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                    activity.setBefore(null);
                    activity.setAfter(newOwner.getDeveloper().getName()
                            + " (" + Util.getDateFormatter().format(new Date(newOwner.getTransferDate())) + ")");
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
            final List<ProductOwnerDTO> origOwners, final List<ProductOwnerDTO> newOwners) {

        List<QuestionableActivityProductDTO> ownerRemovedActivities = new ArrayList<QuestionableActivityProductDTO>();
        if (origOwners != null && origOwners.size() > 0
                && (newOwners == null || newOwners.size() == 0)) {
            //all the origOwners are "removed"
            for (ProductOwnerDTO origOwner : origOwners) {
                QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                activity.setBefore(origOwner.getDeveloper().getName()
                        + " (" + Util.getDateFormatter().format(new Date(origOwner.getTransferDate())) + ")");
                activity.setAfter(null);
                ownerRemovedActivities.add(activity);
            }
        } else if (origOwners != null && origOwners.size() > 0
                && newOwners != null && newOwners.size() > 0) {
            for (ProductOwnerDTO origOwner : origOwners) {
                boolean foundOwner = false;
                for (ProductOwnerDTO newOwner : newOwners) {
                    if (origOwner.getId().equals(newOwner.getId())
                            || (origOwner.getDeveloper().getId().longValue()
                                    == newOwner.getDeveloper().getId().longValue()
                                    && origOwner.getTransferDate().longValue() == newOwner.getTransferDate().longValue())) {
                        foundOwner = true;
                    }
                }
                //orig owner had this item but new did not
                if (!foundOwner) {
                    QuestionableActivityProductDTO activity = new QuestionableActivityProductDTO();
                    activity.setBefore(origOwner.getDeveloper().getName()
                            + " (" + Util.getDateFormatter().format(new Date(origOwner.getTransferDate())) + ")");
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
            final List<ProductOwnerDTO> origOwners, final List<ProductOwnerDTO> newOwners) {

        List<QuestionableActivityProductDTO> ownerEditedActivities = new ArrayList<QuestionableActivityProductDTO>();
        if (origOwners != null && origOwners.size() > 0
                && newOwners != null && newOwners.size() > 0) {
            for (ProductOwnerDTO origOwner : origOwners) {
                boolean ownerEdited = false;
                ProductOwnerDTO matchingNewOwner = null;
                for (ProductOwnerDTO newOwner : newOwners) {
                    if (origOwner.getId().equals(newOwner.getId())) {
                        matchingNewOwner = newOwner;
                        //same id, check if the owner name and date are still the same
                        if (origOwner.getTransferDate().longValue() != newOwner.getTransferDate().longValue()) {
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
                    activity.setBefore(origOwner.getDeveloper().getName()
                            + " (" + Util.getDateFormatter().format(new Date(origOwner.getTransferDate())) + ")");
                    activity.setAfter(matchingNewOwner.getDeveloper().getName()
                            + " (" + Util.getDateFormatter().format(
                                    new Date(matchingNewOwner.getTransferDate())) + ")");
                    ownerEditedActivities.add(activity);
                }
            }
        }
        return ownerEditedActivities;
    }
}
