package gov.healthit.chpl.auth;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice 
public class AuthExceptionControllerAdvice {
	
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseBody
	public String exception(AccessDeniedException e) {
		return "{\"status\":\"access denied\"}";	
	} 
}
