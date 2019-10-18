package gov.healthit.chpl.listener;

import java.util.List;
import java.util.Locale;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.questionableactivity.ListingQuestionableActivityProvider;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Listens for a questionable activity action that requires a user-supplied
 * reason and handles the event that the reason is missing.
 * @author kekey
 *
 */
@Component
@Aspect
public class MissingReasonListener {
    private ErrorMessageUtil errorMessageUtil;
    private CertifiedProductDetailsManager cpdManager;
    private ListingQuestionableActivityProvider listingQuestionableActivityProvider;

    /**
     * Autowired constructor for dependency injection.
     * @param errorMessageUtil - Error message utility class
     * @param cpdManager - CertifiedProductDetailsManager
     * @param listingDao - CertifiedProductDAO
     * @param listingQuestionableActivityProvider - ListingQuestionableActivityProvider
     */
    @Autowired
    public MissingReasonListener(final ErrorMessageUtil errorMessageUtil,
            final CertifiedProductDetailsManager cpdManager, final CertifiedProductDAO listingDao,
            final ListingQuestionableActivityProvider listingQuestionableActivityProvider) {

        this.errorMessageUtil = errorMessageUtil;
        this.cpdManager = cpdManager;
        this.listingQuestionableActivityProvider = listingQuestionableActivityProvider;
    }

    /**
     * Looks for reason for listing update if required.
     * @param updateRequest the listing update object
     * @throws EntityRetrievalException if the listing cannot be found
     * @throws MissingReasonException if a reason was required but is not found
     */
    @Before("execution(* gov.healthit.chpl.web.controller.CertifiedProductController.updateCertifiedProduct(..)) && "
            + "args(updateRequest,..)")
    public void checkReasonProvidedIfRequiredOnListingUpdate(final ListingUpdateRequest updateRequest)
            throws EntityRetrievalException, MissingReasonException {
        CertifiedProductSearchDetails newListing = updateRequest.getListing();
        CertifiedProductSearchDetails origListing = cpdManager.getCertifiedProductDetails(newListing.getId());
        List<QuestionableActivityListingDTO> activities;

        QuestionableActivityListingDTO activity = listingQuestionableActivityProvider
                .check2011EditionUpdated(origListing, newListing);
        if (activity != null && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "updating a 2011 Edition Certified Product"));
        }

        activity = listingQuestionableActivityProvider
                .check2014EditionUpdated(origListing, newListing);
        if (activity != null && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "updating a 2014 Edition Certified Product"));
        }

        activities = listingQuestionableActivityProvider.checkCqmsRemoved(origListing, newListing);
        if (activities.size() > 0 && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "removing a Clinical Quality Measure"));
        }

        activities = listingQuestionableActivityProvider.checkCertificationsRemoved(origListing, newListing);
        if (activities.size() > 0 && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "removing a Certification Criteria"));
        }

        activity = listingQuestionableActivityProvider.checkCertificationStatusUpdated(origListing, newListing);
        if (activity != null
                && newListing.getCurrentStatus().getStatus().getName().toUpperCase(Locale.ENGLISH).equals(
                        CertificationStatusType.Active.getName().toUpperCase(Locale.ENGLISH))
                &&  StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(errorMessageUtil
                    .getMessage("listing.reasonRequired", "changing Certification Status from anything to \"Active\""));
        }
    }

    /**
     * Checks a surveillance delete request to make sure a reason is provided if required.
     * @param surveillanceId surveillance to delete
     * @param requestBody object containing the reason
     * @throws MissingReasonException if the reason is required but not found
     */
    @Before("execution(* gov.healthit.chpl.web.controller.SurveillanceController.deleteSurveillance(..)) && "
            + "args(surveillanceId,requestBody,..)")
    public void checkReasonProvidedIfRequiredOnSurveillanceDelete(final Long surveillanceId,
            final SimpleExplainableAction requestBody)
                    throws MissingReasonException {
        if (surveillanceId != null && (requestBody == null
                ||  StringUtils.isEmpty(requestBody.getReason()))) {
            throw new MissingReasonException(errorMessageUtil.getMessage("surveillance.reasonRequired"));
        }
    }
}
