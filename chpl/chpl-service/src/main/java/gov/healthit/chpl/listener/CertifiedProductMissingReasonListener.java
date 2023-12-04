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
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.questionableactivity.listing.DeletedCertificationsActivity;
import gov.healthit.chpl.questionableactivity.listing.DeletedCqmsActivity;
import gov.healthit.chpl.questionableactivity.listing.NonActiveCertificateEdited;
import gov.healthit.chpl.questionableactivity.listing.UpdateCurrentCertificationStatusActivity;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
@Aspect
public class CertifiedProductMissingReasonListener {
    private static final Logger LOGGER = LogManager.getLogger(CertifiedProductMissingReasonListener.class);

    private ErrorMessageUtil errorMessageUtil;
    private CertifiedProductDetailsManager cpdManager;
    private NonActiveCertificateEdited nonActiveCertificateEditedActivity;
    private DeletedCqmsActivity deletedCqmsActivity;
    private DeletedCertificationsActivity deletedCertificationsActivity;
    private UpdateCurrentCertificationStatusActivity updatedCurrentCertificationStatusActivity;

    @Autowired
    public CertifiedProductMissingReasonListener(ErrorMessageUtil errorMessageUtil,
            CertifiedProductDetailsManager cpdManager, final CertifiedProductDAO listingDao,
            NonActiveCertificateEdited nonActiveCertificateEditedActivity,
            DeletedCqmsActivity deletedCqmsActivity,
            DeletedCertificationsActivity deletedCertificationsActivity,
            UpdateCurrentCertificationStatusActivity updatedCurrentCertificationStatusActivity) {

        this.errorMessageUtil = errorMessageUtil;
        this.cpdManager = cpdManager;
        this.nonActiveCertificateEditedActivity = nonActiveCertificateEditedActivity;
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

        activities = nonActiveCertificateEditedActivity.check(origListing, newListing);
        if (doActivitiesExist(activities)
                && StringUtils.isEmpty(updateRequest.getReason())
                && !updateRequest.isAcknowledgeBusinessErrors()) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "updating a Non-Active Certificate"));
        }

        activities = deletedCqmsActivity.check(origListing, newListing);
        if (doActivitiesExist(activities)
                && StringUtils.isEmpty(updateRequest.getReason())
                && !updateRequest.isAcknowledgeBusinessErrors()) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "removing a Clinical Quality Measure"));
        }

        activities = deletedCertificationsActivity.check(origListing, newListing);
        if (doActivitiesExist(activities)
                && StringUtils.isEmpty(updateRequest.getReason())
                && !updateRequest.isAcknowledgeBusinessErrors()) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "removing a Certification Criteria"));
        }

        if (newListing.getCertificationEvents() != null && newListing.getCertificationEvents().size() != 0) {
            activities = updatedCurrentCertificationStatusActivity.check(origListing, newListing);
            if (doActivitiesExist(activities)
                    && (newListing.getCurrentStatus().getStatus().getName().toUpperCase(Locale.ENGLISH).equals(
                            CertificationStatusType.Active.getName().toUpperCase(Locale.ENGLISH)))
                    && StringUtils.isEmpty(updateRequest.getReason())
                    && !updateRequest.isAcknowledgeBusinessErrors()) {
                throw new MissingReasonException(errorMessageUtil
                        .getMessage("listing.reasonRequired", "changing Certification Status from anything to \"Active\""));
            }
        }
    }

    private Boolean doActivitiesExist(List<QuestionableActivityListing> activities) {
        return activities != null
                && activities.size() > 0
                && activities.get(0) != null;
    }
}
