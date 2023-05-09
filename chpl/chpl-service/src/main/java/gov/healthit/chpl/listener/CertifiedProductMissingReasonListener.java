package gov.healthit.chpl.listener;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certifiedproduct.CertifiedProductDetailsManager;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListing;
import gov.healthit.chpl.questionableactivity.listing.DeletedCertificationsActivity;
import gov.healthit.chpl.questionableactivity.listing.DeletedCqmsActivity;
import gov.healthit.chpl.questionableactivity.listing.UpdateCurrentCertificationStatusActivity;
import gov.healthit.chpl.questionableactivity.listing.Updated2011EditionListingActivity;
import gov.healthit.chpl.questionableactivity.listing.Updated2014EditionListingActivity;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
@Aspect
public class CertifiedProductMissingReasonListener {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductMissingReasonListener.class);

    private ErrorMessageUtil errorMessageUtil;
    private CertifiedProductDetailsManager cpdManager;
    private Updated2011EditionListingActivity updated2011EditionListingActivity;
    private Updated2014EditionListingActivity updated2014EditionListingActivity;
    private DeletedCqmsActivity deletedCqmsActivity;
    private DeletedCertificationsActivity deletedCertificationsActivity;
    private UpdateCurrentCertificationStatusActivity updatedCurrentCertificationStatusActivity;

    @Autowired
    public CertifiedProductMissingReasonListener(ErrorMessageUtil errorMessageUtil,
            CertifiedProductDetailsManager cpdManager, final CertifiedProductDAO listingDao,
            Updated2011EditionListingActivity updated2011EditionListingActivity,
            Updated2014EditionListingActivity updated2014EditionListingActivity,
            DeletedCqmsActivity deletedCqmsActivity,
            DeletedCertificationsActivity deletedCertificationsActivity,
            UpdateCurrentCertificationStatusActivity updatedCurrentCertificationStatusActivity) {

        this.errorMessageUtil = errorMessageUtil;
        this.cpdManager = cpdManager;
        this.updated2011EditionListingActivity = updated2011EditionListingActivity;
        this.updated2014EditionListingActivity = updated2014EditionListingActivity;
        this.deletedCqmsActivity = deletedCqmsActivity;
        this.deletedCertificationsActivity = deletedCertificationsActivity;
        this.updatedCurrentCertificationStatusActivity = updatedCurrentCertificationStatusActivity;
    }

    @Before("execution(* gov.healthit.chpl.manager.CertifiedProductManager+.update(..)) && args(.., updateRequest)")
    public void checkReasonProvidedIfRequiredOnListingUpdate(final ListingUpdateRequest updateRequest)
            throws EntityRetrievalException, MissingReasonException {
        CertifiedProductSearchDetails newListing = updateRequest.getListing();
        CertifiedProductSearchDetails origListing = cpdManager.getCertifiedProductDetails(newListing.getId());
        List<QuestionableActivityListing> activities;

        activities = updated2011EditionListingActivity.check(origListing, newListing);
                if (doActivitiesExist(activities) && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "updating a 2011 Edition Certified Product"));
        }

        activities = updated2014EditionListingActivity.check(origListing, newListing);
        if (doActivitiesExist(activities) && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "updating a 2014 Edition Certified Product"));
        }

        activities = deletedCqmsActivity.check(origListing, newListing);
        if (doActivitiesExist(activities) && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "removing a Clinical Quality Measure"));
        }

        activities = deletedCertificationsActivity.check(origListing, newListing);
        if (doActivitiesExist(activities) && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "removing a Certification Criteria"));
        }

        activities = updatedCurrentCertificationStatusActivity.check(origListing, newListing);
        if (doActivitiesExist(activities)
                && newListing.getCurrentStatus().getStatus().getName().toUpperCase(Locale.ENGLISH).equals(
                        CertificationStatusType.Active.getName().toUpperCase(Locale.ENGLISH))
                && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "changing Certification Status from anything to \"Active\""));
        }
    }

    private Boolean doActivitiesExist(List<QuestionableActivityListing> activities) {
        return activities != null
                && activities.size() > 0
                && activities.get(0) != null;
    }
}
