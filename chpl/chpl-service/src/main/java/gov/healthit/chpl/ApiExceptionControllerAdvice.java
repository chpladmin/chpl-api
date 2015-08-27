package gov.healthit.chpl;


import gov.healthit.chpl.auth.json.ErrorJSONObject;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;

import org.hibernate.exception.SQLGrammarException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice 
public class ApiExceptionControllerAdvice {
	
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
	
	
	@ExceptionHandler(SQLGrammarException.class)
	public ResponseEntity<ErrorJSONObject> exception(SQLGrammarException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(
				"Data error. Please check your input data and try again. If this issue persists "
				+ "please contact the CHPL administrator."
				), HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorJSONObject> exception(Exception e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
