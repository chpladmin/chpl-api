package gov.healthit.chpl.questionableActivity;

import java.util.List;
import java.util.Locale;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
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

@Component
@Aspect
public class QuestionableActivityAspect {
    private MessageSource messageSource;
    private CertifiedProductDetailsManager cpdManager;
    private ListingQuestionableActivityProvider listingQuestionableActivityProvider;

    /**
     * Autowired constructor for dependency injection.
     * @param messageSource - MessageSource
     * @param cpdManager - CertifiedProductDetailsManager
     * @param listingDao - CertifiedProductDAO
     * @param listingQuestionableActivityProvider - ListingQuestionableActivityProvider
     */
    @Autowired
    public QuestionableActivityAspect(final MessageSource messageSource,
            final CertifiedProductDetailsManager cpdManager, final CertifiedProductDAO listingDao,
            final ListingQuestionableActivityProvider listingQuestionableActivityProvider) {

        this.messageSource = messageSource;
        this.cpdManager = cpdManager;
        this.listingQuestionableActivityProvider = listingQuestionableActivityProvider;
    }

    /**
     * @param updateRequest - ListingUpdateRequest
     * @throws EntityRetrievalException
     * @throws MissingReasonException
     */
    @Before("execution(* "
            + "gov.healthit.chpl.web.controller.CertifiedProductController.updateCertifiedProductDeprecated(..)) && "
            + "args(updateRequest,..)")
    public void checkReasonProvidedIfRequiredOnListingUpdateDeprecated(final ListingUpdateRequest updateRequest)
            throws EntityRetrievalException, MissingReasonException {
        checkReasonProvidedIfRequiredOnListingUpdate(updateRequest);
    }

    /**
     * @param updateRequest
     * @throws EntityRetrievalException
     * @throws MissingReasonException
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
            throw new MissingReasonException(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("listing.reasonRequired"),
                    LocaleContextHolder.getLocale())));
        }

        activities = listingQuestionableActivityProvider.checkCqmsRemoved(origListing, newListing);
        if (activities.size() > 0 && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("listing.reasonRequired"),
                    LocaleContextHolder.getLocale())));
        }

        activities = listingQuestionableActivityProvider.checkCertificationsRemoved(origListing, newListing);
        if (activities.size() > 0 && StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("listing.reasonRequired"),
                    LocaleContextHolder.getLocale())));
        }

        activity = listingQuestionableActivityProvider.checkCertificationStatusUpdated(origListing, newListing);
        if (activity != null
                && newListing.getCurrentStatus().getStatus().getName().toUpperCase(Locale.ENGLISH).equals(
                        CertificationStatusType.Active.getName().toUpperCase(Locale.ENGLISH))
                &&  StringUtils.isEmpty(updateRequest.getReason())) {
            throw new MissingReasonException(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("listing.reasonRequired"),
                    LocaleContextHolder.getLocale()),
                    origListing.getCurrentStatus().getStatus().getName()));
        }
    }

    /**
     * @param surveillanceId
     * @param requestBody
     * @throws MissingReasonException
     */
    @Before("execution(* gov.healthit.chpl.web.controller.SurveillanceController.deleteSurveillance(..)) && "
            + "args(surveillanceId,requestBody,..)")
    public void checkReasonProvidedIfRequiredOnSurveillanceUpdate(final Long surveillanceId,
            final SimpleExplainableAction requestBody)
                    throws MissingReasonException {
        if (surveillanceId != null && (requestBody == null
                ||  StringUtils.isEmpty(requestBody.getReason()))) {
            throw new MissingReasonException(String.format(messageSource.getMessage(
                    new DefaultMessageSourceResolvable("surveillance.reasonRequired"),
                    LocaleContextHolder.getLocale())));
        }
    }
}
