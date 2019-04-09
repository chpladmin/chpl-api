package gov.healthit.chpl;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import gov.healthit.chpl.domain.error.ErrorResponse;
import gov.healthit.chpl.domain.error.ObjectMissingValidationErrorResponse;
import gov.healthit.chpl.domain.error.ObjectsMissingValidationErrorResponse;
import gov.healthit.chpl.domain.error.ValidationErrorResponse;
import gov.healthit.chpl.exception.CertificationBodyAccessException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ObjectsMissingValidationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;

/**
 * Catch thrown exceptions to return the proper response code and message back to the client.
 * @author kekey
 *
 */
@ControllerAdvice
public class ApiExceptionControllerAdvice {
    private static final Logger LOGGER = LogManager.getLogger(ApiExceptionControllerAdvice.class);

    @ExceptionHandler(EntityRetrievalException.class)
    public ResponseEntity<ErrorResponse> exception(final EntityRetrievalException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserRetrievalException.class)
    public ResponseEntity<ErrorResponse> exception(final UserRetrievalException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(EntityCreationException.class)
    public ResponseEntity<ErrorResponse> exception(final EntityCreationException e) {
        LOGGER.error(e.getMessage(), e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidArgumentsException.class)
    public ResponseEntity<ErrorResponse> exception(final InvalidArgumentsException e) {
        LOGGER.error(e.getMessage(), e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> exception(final IllegalArgumentException e) {
        LOGGER.error(e.getMessage(), e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> typeMismatchException(final TypeMismatchException e) {
        LOGGER.error(e.getMessage(), e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AddressException.class)
    public ResponseEntity<ErrorResponse> exception(final AddressException e) {
        LOGGER.error("Could not send email", e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Could not send email. " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UpdateTestingLabException.class)
    public ResponseEntity<ErrorResponse> exception(final UpdateTestingLabException e) {
        LOGGER.error("Could not update testing lab - access denied.");
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Access Denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SurveillanceAuthorityAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> exception(final SurveillanceAuthorityAccessDeniedException e) {
        LOGGER.error("Could not update surveillance activity - access denied.");
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Access Denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UpdateCertifiedBodyException.class)
    public ResponseEntity<ErrorResponse> exception(final UpdateCertifiedBodyException e) {
        LOGGER.error("Could not update ACB - access denied.");
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Access Denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ErrorResponse> exception(final MessagingException e) {
        LOGGER.error("Could not send email", e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Could not send email. " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> exception(final ValidationException e) {
        ValidationErrorResponse error = new ValidationErrorResponse();
        error.setErrorMessages(e.getErrorMessages());
        error.setWarningMessages(e.getWarningMessages());
        return new ResponseEntity<ValidationErrorResponse>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Return bad request for thrown ObjectsMissingValidationException including contact information.
     * @param e the thrown exception
     * @return an http response with appropriate error code.
     */
    @ExceptionHandler(ObjectsMissingValidationException.class)
    public ResponseEntity<ObjectsMissingValidationErrorResponse> exception(
            final ObjectsMissingValidationException e) {
        ObjectsMissingValidationErrorResponse errorContainer = new ObjectsMissingValidationErrorResponse();
        if (e.getExceptions() != null) {
            for (ObjectMissingValidationException currEx : e.getExceptions()) {
                ObjectMissingValidationErrorResponse error = new ObjectMissingValidationErrorResponse();
                error.setErrorMessages(currEx.getErrorMessages());
                error.setWarningMessages(currEx.getWarningMessages());
                error.setContact(currEx.getContact());
                error.setObjectId(currEx.getObjectId());
                errorContainer.getErrors().add(error);
            }
        }

        return new ResponseEntity<ObjectsMissingValidationErrorResponse>(errorContainer, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectMissingValidationException.class)
    public ResponseEntity<ObjectMissingValidationErrorResponse> exception(final ObjectMissingValidationException e) {
        ObjectMissingValidationErrorResponse error = new ObjectMissingValidationErrorResponse();
        error.setErrorMessages(e.getErrorMessages());
        error.setWarningMessages(e.getWarningMessages());
        error.setContact(e.getContact());
        error.setObjectId(e.getObjectId());
        return new ResponseEntity<ObjectMissingValidationErrorResponse>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CertificationBodyAccessException.class)
    public ResponseEntity<ErrorResponse> exception(final CertificationBodyAccessException e) {
        LOGGER.error("Caught ACB access exception.", e);
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage() != null ? e.getMessage() : "Unauthorized ACB Access."),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingReasonException.class)
    public ResponseEntity<ErrorResponse> exception(final MissingReasonException e) {
        LOGGER.error("Caught missing reason exception.", e);
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage() != null ? e.getMessage()
                        : "A reason is required to perform this action."),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ErrorResponse> exceptionHandler(final IOException e, final HttpServletRequest request) {
        if (StringUtils.containsIgnoreCase(ExceptionUtils.getRootCauseMessage(e), "Broken pipe")) {
            LOGGER.info("Broke Pipe IOException occurred: " + request.getMethod() + " " + request.getRequestURL());
            LOGGER.error(e.getMessage(), e);
            return null; //socket is closed, cannot return any response
        } else {
            return new ResponseEntity<ErrorResponse>(
                    new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> exception(final Exception e) {
        LOGGER.error("Caught exception.", e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
