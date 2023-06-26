package gov.healthit.chpl;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.collections.api.factory.SortedSets;
import org.redisson.client.RedisTimeoutException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import gov.healthit.chpl.auth.ChplAccountEmailNotConfirmedException;
import gov.healthit.chpl.domain.error.ErrorResponse;
import gov.healthit.chpl.domain.error.ObjectMissingValidationErrorResponse;
import gov.healthit.chpl.domain.error.ObjectsMissingValidationErrorResponse;
import gov.healthit.chpl.domain.error.ValidationErrorResponse;
import gov.healthit.chpl.exception.CertificationBodyAccessException;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.JiraRequestFailedException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ObjectsMissingValidationException;
import gov.healthit.chpl.exception.UserAccountExistsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@RestControllerAdvice
@Log4j2
public class ApiExceptionControllerAdvice {

    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public ApiExceptionControllerAdvice(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    @ExceptionHandler(NotImplementedException.class)
    public ResponseEntity<ErrorResponse> exception(NotImplementedException e) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()), HttpStatus.NOT_IMPLEMENTED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> exception(AccessDeniedException e) {
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage() == null ? "Access Denied" : e.getMessage()), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(JiraRequestFailedException.class)
    public ResponseEntity<ErrorResponse> exception(JiraRequestFailedException e) {
        LOGGER.error(e.getMessage());
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse("Direct Review information is not currently available, please check back later."),
                HttpStatus.NO_CONTENT);
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

    @ExceptionHandler(UpdateTestingLabException.class)
    public ResponseEntity<ErrorResponse> exception(UpdateTestingLabException e) {
        LOGGER.error("Could not update testing lab - access denied.");
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Access Denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UpdateCertifiedBodyException.class)
    public ResponseEntity<ErrorResponse> exception(UpdateCertifiedBodyException e) {
        LOGGER.error("Could not update ACB - access denied.");
        return new ResponseEntity<ErrorResponse>(new ErrorResponse("Access Denied"), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(EmailNotSentException.class)
    public ResponseEntity<ErrorResponse> exception(EmailNotSentException e) {
        return new ResponseEntity<ErrorResponse>(new ErrorResponse(e.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> exception(ValidationException e) {
        return new ResponseEntity<ValidationErrorResponse>(ValidationErrorResponse.builder()
                .errorMessages(e.getErrorMessages())
                .businessErrorMessages(e.getBusinessErrorMessages())
                .dataErrorMessages(e.getDataErrorMessages())
                .warningMessages(e.getWarningMessages())
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectsMissingValidationException.class)
    public ResponseEntity<ObjectsMissingValidationErrorResponse> exception(
            ObjectsMissingValidationException e) {
        ObjectsMissingValidationErrorResponse errorContainer = new ObjectsMissingValidationErrorResponse();
        if (e.getExceptions() != null) {
            for (ObjectMissingValidationException currEx : e.getExceptions()) {
                ObjectMissingValidationErrorResponse error = ObjectMissingValidationErrorResponse.builder()
                        .errorMessages(currEx.getErrorMessages())
                        .warningMessages(currEx.getWarningMessages())
                        .contact(currEx.getUser())
                        .objectId(currEx.getObjectId())
                        .build();
                errorContainer.getErrors().add(error);
            }
        }

        return new ResponseEntity<ObjectsMissingValidationErrorResponse>(errorContainer, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ObjectMissingValidationException.class)
    public ResponseEntity<ObjectMissingValidationErrorResponse> exception(ObjectMissingValidationException e) {
        ObjectMissingValidationErrorResponse error = ObjectMissingValidationErrorResponse.builder()
                .errorMessages(e.getErrorMessages())
                .warningMessages(e.getWarningMessages())
                .contact(e.getUser())
                .objectId(e.getObjectId())
                .build();
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
    public ResponseEntity<ValidationErrorResponse> exception(MissingReasonException e) {
        LOGGER.error("Caught missing reason exception.", e);
        return new ResponseEntity<ValidationErrorResponse>(ValidationErrorResponse.builder()
                .errorMessages(SortedSets.immutable.of(e.getMessage() != null ? e.getMessage() : "A reason is required to perform this action."))
                .businessErrorMessages(SortedSets.immutable.of(e.getMessage() != null ? e.getMessage() : "A reason is required to perform this action."))
                .build(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ChplAccountEmailNotConfirmedException.class)
    public ResponseEntity<ErrorResponse> exception(ChplAccountEmailNotConfirmedException e) {
        LOGGER.error(String.format("User's email [%s] is not confirmed. Resent confirm address email.", e.getEmailAddress()));
        return ResponseEntity
                .status(ChplHttpStatus.RESENT_USER_CONFIRMATION_EMAIL.value())
                .body(new ErrorResponse(ChplHttpStatus.RESENT_USER_CONFIRMATION_EMAIL.getReasonPhrase()));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(HttpClientErrorException e, HttpServletRequest request) {
        LOGGER.error("HttpClientErrorException: " + e.getMessage(), e);
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage() != null ? e.getMessage()
                        : "The request could not be completed. Please check your credentials and the connection to the server."),
                e.getStatusCode());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(HttpServerErrorException e, HttpServletRequest request) {
        LOGGER.error("HttpServerErrorException: " + e.getMessage(), e);
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse(e.getMessage() != null ? e.getMessage()
                        : "The server encountered an error completing the request."),
                e.getStatusCode());
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ErrorResponse> exceptionHandler(IOException e, HttpServletRequest request) {
        if (StringUtils.containsIgnoreCase(ExceptionUtils.getRootCauseMessage(e), "Broken pipe")) {
            LOGGER.warn("Broke Pipe IOException occurred: " + request.getMethod() + " " + request.getRequestURL());
            return null; // socket is closed, cannot return any response
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

    @ExceptionHandler(RedisTimeoutException.class)
    public ResponseEntity<ValidationErrorResponse> exception(RedisTimeoutException e) {
        LOGGER.error(e.getMessage(), e);
        return new ResponseEntity<ValidationErrorResponse>(ValidationErrorResponse.builder()
                .errorMessages(SortedSets.immutable.of(errorMessageUtil.getMessage("redis.connection.timeout")))
                .businessErrorMessages(SortedSets.immutable.of(errorMessageUtil.getMessage("redis.connection.timeout")))
                .build(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
