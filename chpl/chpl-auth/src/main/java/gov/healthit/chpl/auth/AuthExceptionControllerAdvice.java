package gov.healthit.chpl.auth;

import javax.servlet.ServletException;

import gov.healthit.chpl.auth.json.ErrorJSONObject;
import gov.healthit.chpl.auth.jwt.JWTCreationException;
import gov.healthit.chpl.auth.permission.UserPermissionRetrievalException;
import gov.healthit.chpl.auth.user.UserManagementException;
import gov.healthit.chpl.auth.user.UserRetrievalException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice 
public class AuthExceptionControllerAdvice {
	
	@ExceptionHandler(AccessDeniedException.class)
	public @ResponseBody ErrorJSONObject exception(AccessDeniedException e) {
		return new ErrorJSONObject("Access denied.");
	}
	
	@ExceptionHandler(UserRetrievalException.class)
	public @ResponseBody ErrorJSONObject exception(UserRetrievalException e) {
		return new ErrorJSONObject(e.getMessage());
	}
	
	@ExceptionHandler(UserPermissionRetrievalException.class)
	public @ResponseBody ErrorJSONObject exception(UserPermissionRetrievalException e) {
		return new ErrorJSONObject(e.getMessage());
	}
	
	@ExceptionHandler(UserManagementException.class)
	public @ResponseBody ErrorJSONObject exception(UserManagementException e) {
		return new ErrorJSONObject(e.getMessage());
	}
	
	@ExceptionHandler(JWTCreationException.class)
	public @ResponseBody ErrorJSONObject exception(JWTCreationException e) {
		return new ErrorJSONObject(e.getMessage());
	}
	
	//JWTCreationException
	
	// Unfortunately, looks like we might not be able to use this
	// for ServletException thrown by filter
	/*
	@ExceptionHandler(ServletException.class)
	public @ResponseBody ErrorJSONObject exception(ServletException e) {
		return new ErrorJSONObject(e.getMessage());
	}	
	*/
	
}
