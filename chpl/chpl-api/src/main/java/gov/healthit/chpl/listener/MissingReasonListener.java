package gov.healthit.chpl.listener;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.SimpleExplainableAction;
import gov.healthit.chpl.exception.MissingReasonException;
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

    @Autowired
    public MissingReasonListener(final ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
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
