package gov.healthit.chpl.listener;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.SimpleExplainableAction;
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
