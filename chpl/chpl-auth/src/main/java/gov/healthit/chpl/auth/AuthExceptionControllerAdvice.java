package gov.healthit.chpl.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import gov.healthit.chpl.auth.json.ErrorJSONObject;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserCreationException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

@ControllerAdvice
public class AuthExceptionControllerAdvice {

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorJSONObject> exception(AccessDeniedException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage() == null ? "Access Denied" : e.getMessage()), 
				HttpStatus.UNAUTHORIZED);
	}

	@ExceptionHandler(UserCreationException.class)
	public ResponseEntity<ErrorJSONObject> exception(UserCreationException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(UserPermissionRetrievalException.class)
	public ResponseEntity<ErrorJSONObject> exception(UserPermissionRetrievalException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(UserManagementException.class)
	public ResponseEntity<ErrorJSONObject> exception(UserManagementException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(JWTCreationException.class)
	public ResponseEntity<ErrorJSONObject> exception(JWTCreationException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()),
				HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorJSONObject> exception(BadCredentialsException e) {
		return new ResponseEntity<ErrorJSONObject>(new ErrorJSONObject(e.getMessage()), HttpStatus.UNAUTHORIZED);
	}

}
