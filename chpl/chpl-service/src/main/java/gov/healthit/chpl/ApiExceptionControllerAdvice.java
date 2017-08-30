package gov.healthit.chpl;


import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import gov.healthit.chpl.auth.json.ErrorJSONObject;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.error.ObjectMissingValidationErrorJSONObject;
import gov.healthit.chpl.domain.error.ObjectsMissingValidationErrorJSONObject;
import gov.healthit.chpl.domain.error.ValidationErrorJSONObject;
import gov.healthit.chpl.manager.impl.SurveillanceAuthorityAccessDeniedException;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;
import gov.healthit.chpl.web.controller.CertificationBodyAccessException;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;
import gov.healthit.chpl.web.controller.exception.ObjectMissingValidationException;
import gov.healthit.chpl.web.controller.exception.ObjectsMissingValidationException;
import gov.healthit.chpl.web.controller.exception.ValidationException;


@ControllerAdvice
public class ApiExceptionControllerAdvice {
	private static final Logger logger = LogManager.getLogger(ApiExceptionControllerAdvice.class);

	@ExceptionHandler(EntityRetrievalException.class)
	public ResponseEntity<ErrorJSONObject> exception(EntityRetrievalException e) {
		logger.error(e.getMessage(), e);
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(EntityCreationException.class)
	public ResponseEntity<ErrorJSONObject> exception(EntityCreationException e) {
		logger.error(e.getMessage(), e);
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(InvalidArgumentsException.class)
	public ResponseEntity<ErrorJSONObject> exception(InvalidArgumentsException e) {
		logger.error(e.getMessage(), e);
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(TypeMismatchException.class)
	public ResponseEntity<ErrorJSONObject> typeMismatchException(TypeMismatchException e) {
		logger.error(e.getMessage(), e);
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(AddressException.class)
	public ResponseEntity<ErrorJSONObject> exception(AddressException e) {
		logger.error("Could not send email", e);
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Could not send email. " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(UpdateTestingLabException.class )
	public ResponseEntity<ErrorJSONObject> exception(UpdateTestingLabException e) {
		logger.error("Could not update testing lab - access denied.");
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Access Denied"), HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(SurveillanceAuthorityAccessDeniedException.class )
	public ResponseEntity<ErrorJSONObject> exception(SurveillanceAuthorityAccessDeniedException e) {
		logger.error("Could not update surveillance activity - access denied.");
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Access Denied"), HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(UpdateCertifiedBodyException.class )
	public ResponseEntity<ErrorJSONObject> exception(UpdateCertifiedBodyException e) {
		logger.error("Could not update ACB - access denied.");
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Access Denied"), HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(MessagingException.class)
	public ResponseEntity<ErrorJSONObject> exception(MessagingException e) {
		logger.error("Could not send email", e);
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Could not send email. " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ValidationErrorJSONObject> exception(ValidationException e) {
		ValidationErrorJSONObject error = new ValidationErrorJSONObject();
		error.setErrorMessages(e.getErrorMessages());
		error.setWarningMessages(e.getWarningMessages());
		return new ResponseEntity<ValidationErrorJSONObject>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(ObjectsMissingValidationException.class)
	public ResponseEntity<ObjectsMissingValidationErrorJSONObject> exception(ObjectsMissingValidationException e) {
		ObjectsMissingValidationErrorJSONObject errorContainer = new ObjectsMissingValidationErrorJSONObject();
		if(e.getExceptions() != null) {
			for(ObjectMissingValidationException currEx : e.getExceptions()) {
				ObjectMissingValidationErrorJSONObject error = new ObjectMissingValidationErrorJSONObject();
				error.setErrorMessages(currEx.getErrorMessages());
				error.setWarningMessages(currEx.getWarningMessages());
				error.setContact(currEx.getContact());
				error.setObjectId(currEx.getObjectId());
				errorContainer.getErrors().add(error);
			}
		}
		
		return new ResponseEntity<ObjectsMissingValidationErrorJSONObject>(errorContainer, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(ObjectMissingValidationException.class)
	public ResponseEntity<ObjectMissingValidationErrorJSONObject> exception(ObjectMissingValidationException e) {
		ObjectMissingValidationErrorJSONObject error = new ObjectMissingValidationErrorJSONObject();
		error.setErrorMessages(e.getErrorMessages());
		error.setWarningMessages(e.getWarningMessages());
		error.setContact(e.getContact());
		error.setObjectId(e.getObjectId());
		return new ResponseEntity<ObjectMissingValidationErrorJSONObject>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(CertificationBodyAccessException.class)
	public ResponseEntity<ErrorJSONObject> exception(CertificationBodyAccessException e) {
		logger.error("Caught ACB access exception.", e);		
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage() != null ? e.getMessage() : "Unauthorized ACB Access."), HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorJSONObject> exception(Exception e) {
		logger.error("Caught exception.", e);		
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
}
