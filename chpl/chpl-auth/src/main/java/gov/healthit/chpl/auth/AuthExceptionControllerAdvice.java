package gov.healthit.chpl.auth;

import javax.servlet.ServletException;

import gov.healthit.chpl.auth.json.ErrorJSONObject;

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
	
	
	// Unfortunately, looks like we might not be able to use this
	// for ServletException thrown by filter
	/*
	@ExceptionHandler(ServletException.class)
	public @ResponseBody ErrorJSONObject exception(ServletException e) {
		return new ErrorJSONObject(e.getMessage());
	}	
	*/
	
}
