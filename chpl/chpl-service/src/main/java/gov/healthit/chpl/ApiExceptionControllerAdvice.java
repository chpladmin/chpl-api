package gov.healthit.chpl;


import gov.healthit.chpl.auth.json.ErrorJSONObject;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ValidationErrorJSONObject;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;
import gov.healthit.chpl.web.controller.ValidationException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
public class ApiExceptionControllerAdvice {
	private static final Logger logger = LogManager.getLogger(ApiExceptionControllerAdvice.class);

	@ExceptionHandler(EntityRetrievalException.class)
	public ResponseEntity<ErrorJSONObject> exception(EntityRetrievalException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(EntityCreationException.class)
	public ResponseEntity<ErrorJSONObject> exception(EntityCreationException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(InvalidArgumentsException.class)
	public ResponseEntity<ErrorJSONObject> exception(InvalidArgumentsException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(AddressException.class)
	public ResponseEntity<ErrorJSONObject> exception(AddressException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Could not send email. " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(UpdateTestingLabException.class )
	public ResponseEntity<ErrorJSONObject> exception(UpdateTestingLabException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Access Denied"), HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(UpdateCertifiedBodyException.class )
	public ResponseEntity<ErrorJSONObject> exception(UpdateCertifiedBodyException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Access Denied"), HttpStatus.FORBIDDEN);
	}
	
	@ExceptionHandler(MessagingException.class)
	public ResponseEntity<ErrorJSONObject> exception(MessagingException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject("Could not send email. " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(ValidationException.class)
	public ResponseEntity<ValidationErrorJSONObject> exception(ValidationException e) {
		ValidationErrorJSONObject error = new ValidationErrorJSONObject();
		error.setErrorMessages(e.getErrorMessages());
		error.setWarningMessages(e.getWarningMessages());
		return new ResponseEntity<ValidationErrorJSONObject>(error, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorJSONObject> exception(Exception e) {
		e.printStackTrace();
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
}
