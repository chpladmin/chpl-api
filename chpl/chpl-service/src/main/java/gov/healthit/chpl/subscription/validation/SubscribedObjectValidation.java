package gov.healthit.chpl.subscription.validation;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.manager.rules.ValidationRule;
import gov.healthit.chpl.search.domain.ListingSearchResult;

public class SubscribedObjectValidation extends ValidationRule<SubscriptionRequestValidationContext> {

    @Override
    public boolean isValid(SubscriptionRequestValidationContext context) {
        if (context.getSubscriptionRequest().getSubscribedObjectId() == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.objectIdRequired"));
            return false;
        }

        Long listingTypeId = context.getLookupUtil().getListingObjectTypeId();
        Long developerTypeId = context.getLookupUtil().getDeveloperObjectTypeId();
        Long productTypeId = context.getLookupUtil().getProductObjectTypeId();

        Long objectTypeId = context.getSubscriptionRequest().getSubscribedObjectTypeId();
        if (objectTypeId.equals(listingTypeId)) {
            return isListingIdValid(context.getSubscriptionRequest().getSubscribedObjectId(), context);
        } else if (objectTypeId.equals(developerTypeId)) {
            return isDeveloperIdValid(context.getSubscriptionRequest().getSubscribedObjectId(), context);
        } else if (objectTypeId.equals(productTypeId)) {
            return isProductIdValid(context.getSubscriptionRequest().getSubscribedObjectId(), context);
        }
        return true;
    }

    private boolean isListingIdValid(Long listingId, SubscriptionRequestValidationContext context) {
        ListingSearchResult listingWithId = null;
        try {
            listingWithId = context.getListingSearchService().findListing(
                    context.getSubscriptionRequest().getSubscribedObjectId());
        } catch (Exception ex) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.objectIdInvalid",
                    listingId));
            return false;
        }

        if (listingWithId == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.objectIdInvalid",
                    listingId));
            return false;
        }
        return true;
    }

    private boolean isDeveloperIdValid(Long developerId, SubscriptionRequestValidationContext context) {
        Developer developer = null;
        try {
            developer = context.getDeveloperDao().getSimpleDeveloperById(developerId, false);
        } catch (Exception ex) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.objectIdInvalid",
                    developerId));
            return false;
        }

        if (developer == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.objectIdInvalid",
                    developerId));
            return false;
        }
        return true;
    }

    private boolean isProductIdValid(Long productId, SubscriptionRequestValidationContext context) {
        Product product = null;
        try {
            product = context.getProductDao().getById(productId);
        } catch (Exception ex) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.objectIdInvalid",
                    productId));
            return false;
        }

        if (product == null) {
            getMessages().add(context.getErrorMessageUtil().getMessage("subscription.objectIdInvalid",
                    productId));
            return false;
        }
        return true;
    }
}
