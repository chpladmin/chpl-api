package gov.healthit.chpl;

import java.io.IOException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ObjectsMissingValidationException;
import gov.healthit.chpl.exception.UserAccountExistsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;
import lombok.extern.log4j.Log4j2;

/**
 * Catch thrown exceptions to return the proper response code and message back to the client.
 * @author kekey
 *
 */
@ControllerAdvice
@Log4j2
public class ApiExceptionControllerAdvice {
    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<ErrorResponse> exception(NotImplementedException e) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> exception(AccessDeniedException e) {
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage() == null ? "Access Denied" : e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EntityRetrievalException.class)
    public ResponseEntity<ErrorResponse> exception(EntityRetrievalException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UserRetrievalException.class)
    public ResponseEntity<ErrorResponse> exception(UserRetrievalException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MultipleUserAccountsException.class)
    public ResponseEntity<ErrorResponse> exception(MultipleUserAccountsException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAccountExistsException.class)
    public ResponseEntity<ErrorResponse> exception(UserAccountExistsException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EntityCreationException.class)
    public ResponseEntity<ErrorResponse> exception(EntityCreationException e) {
        LOGGER.error(e.getMessage(), e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidArgumentsException.class)
    public ResponseEntity<ErrorResponse> exception(InvalidArgumentsException e) {
        LOGGER.error(e.getMessage(), e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> exception(IllegalArgumentException e) {
        LOGGER.error(e.getMessage(), e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ErrorResponse> typeMismatchException(TypeMismatchException e) {
        LOGGER.error(e.getMessage(), e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AddressException.class)
    public ResponseEntity<ErrorResponse> exception(AddressException e) {
        LOGGER.error("Could not send email", e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Could not send email. " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(UpdateTestingLabException.class)
    public ResponseEntity<ErrorResponse> exception(UpdateTestingLabException e) {
        LOGGER.error("Could not update testing lab - access denied.");
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Access Denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(SurveillanceAuthorityAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> exception(SurveillanceAuthorityAccessDeniedException e) {
        LOGGER.error("Could not update surveillance activity - access denied.");
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Access Denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UpdateCertifiedBodyException.class)
    public ResponseEntity<ErrorResponse> exception(UpdateCertifiedBodyException e) {
        LOGGER.error("Could not update ACB - access denied.");
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Access Denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<ErrorResponse> exception(MessagingException e) {
        LOGGER.error("Could not send email", e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Could not send email. " + e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> exception(ValidationException e) {
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
            ObjectsMissingValidationException e) {
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
    public ResponseEntity<ObjectMissingValidationErrorResponse> exception(ObjectMissingValidationException e) {
        ObjectMissingValidationErrorResponse error = new ObjectMissingValidationErrorResponse();
        error.setErrorMessages(e.getErrorMessages());
        error.setWarningMessages(e.getWarningMessages());
        error.setContact(e.getContact());
        error.setObjectId(e.getObjectId());
        return new ResponseEntity<ObjectMissingValidationErrorResponse>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CertificationBodyAccessException.class)
    public ResponseEntity<ErrorResponse> exception(CertificationBodyAccessException e) {
        LOGGER.error("Caught ACB access exception.", e);
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage() != null ? e.getMessage() : "Unauthorized ACB Access."),
                HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MissingReasonException.class)
    public ResponseEntity<ErrorResponse> exception(MissingReasonException e) {
        LOGGER.error("Caught missing reason exception.", e);
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage() != null ? e.getMessage()
                        : "A reason is required to perform this action."),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IOException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ResponseEntity<ErrorResponse> exceptionHandler(IOException e, HttpServletRequest request) {
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
    public ResponseEntity<ErrorResponse> exception(Exception e) {
        LOGGER.error("Caught exception.", e);
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
