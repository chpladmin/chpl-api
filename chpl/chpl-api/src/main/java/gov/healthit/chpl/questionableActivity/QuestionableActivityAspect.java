package gov.healthit.chpl.questionableActivity;

import java.util.List;
import java.util.Locale;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.dao.QuestionableActivityDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityTriggerDTO;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.manager.CertifiedProductDetailsManager;
import gov.healthit.chpl.manager.QuestionableActivityManager;
import gov.healthit.chpl.questionableactivity.CertificationResultQuestionableActivityProvider;
import gov.healthit.chpl.questionableactivity.ListingQuestionableActivityProvider;

@Component
@Aspect
public class QuestionableActivityAspect implements EnvironmentAware {
    private Environment env;
    private MessageSource messageSource;
    private CertifiedProductDetailsManager cpdManager;
    private QuestionableActivityDAO questionableActivityDao;
    private CertifiedProductDAO listingDao;
    private ListingQuestionableActivityProvider listingQuestionableActivityProvider;
    private QuestionableActivityManager questionableActivityManager;
    private List<QuestionableActivityTriggerDTO> triggerTypes;
    private long listingActivityThresholdMillis = -1;
    private static final long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;

    @Autowired
    public QuestionableActivityAspect(final Environment env, final MessageSource messageSource,
            final CertifiedProductDetailsManager cpdManager, final QuestionableActivityDAO questionableActivityDao,
            final CertifiedProductDAO listingDao,
            final ListingQuestionableActivityProvider listingQuestionableActivityProvider,
            final CertificationResultQuestionableActivityProvider certResultQuestionableActivityProvider,
            final QuestionableActivityManager questionableActivityManager) {

        this.env = env;
        this.messageSource = messageSource;
        this.cpdManager = cpdManager;
        this.questionableActivityDao = questionableActivityDao;
        this.listingDao = listingDao;
        this.listingQuestionableActivityProvider = listingQuestionableActivityProvider;
        this.questionableActivityManager = questionableActivityManager;
    }

    @Override
    public void setEnvironment(final Environment e) {
        this.env = e;
        String activityThresholdDaysStr = env.getProperty("questionableActivityThresholdDays");
        int activityThresholdDays = Integer.parseInt(activityThresholdDaysStr);
        listingActivityThresholdMillis = activityThresholdDays * MILLIS_PER_DAY;

        triggerTypes = questionableActivityDao.getAllTriggers();
    }

    @Before("execution(* "
            + "gov.healthit.chpl.web.controller.CertifiedProductController.updateCertifiedProductDeprecated(..)) && "
            + "args(updateRequest,..)")
    public void checkReasonProvidedIfRequiredOnListingUpdateDeprecated(final ListingUpdateRequest updateRequest)
            throws EntityRetrievalException, MissingReasonException {
        checkReasonProvidedIfRequiredOnListingUpdate(updateRequest);
    }

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
