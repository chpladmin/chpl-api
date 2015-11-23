package gov.healthit.chpl.registration;

import gov.healthit.chpl.auth.json.ErrorJSONObject;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class APIKeyAuthenticationFilter extends GenericFilterBean {
	
	
	public APIKeyAuthenticationFilter(){}
	
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		
		System.out.println(request.getPathInfo());
		System.out.println(request.getPathTranslated());
		System.out.println(request.getRequestURI());/*We want this one*/
		System.out.println(request.getRequestURL());
		/*
		 * TODO: How do we get query params?
		 */
		
		String key = request.getHeader("API-Key");
		
		if (key == null){
			// Don't continue. 
			ErrorJSONObject errorObj = new ErrorJSONObject("API key must be presented in order to use this API");
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(errorObj);
			res.getOutputStream().write(json.getBytes());
			
		} else {
			chain.doFilter(req, res); //continue
		}
	}
	
}
