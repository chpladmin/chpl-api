package gov.healthit.chpl.registration;

import gov.healthit.chpl.auth.json.ErrorJSONObject;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.manager.ApiKeyManager;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class APIKeyAuthenticationFilter extends GenericFilterBean {
	
	private static final String[] ALLOWED_REQUEST_PATHS = {"/api-docs"};
	
	@Autowired
	private ApiKeyManager apiKeyManager;
	
	public APIKeyAuthenticationFilter(ApiKeyManager apiKeyManager){
		this.apiKeyManager = apiKeyManager;
	}
	
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		
		String requestPath;
		if (request.getQueryString() == null){
			requestPath = request.getRequestURI();
		} else {
			requestPath = request.getRequestURI() + "?" + request.getQueryString();
		}
		
		String key = null;
		String keyFromHeader = request.getHeader("API-Key");
		String keyFromParam = request.getParameter("api_key");
		
		if (keyFromHeader == keyFromParam){
			key = keyFromHeader;
		} else {
			
			if (keyFromHeader == null){
				key = keyFromParam;
			} else if (keyFromParam == null){
				key = keyFromHeader;
			} else {
				// Keys don't match. Don't continue. 
				ErrorJSONObject errorObj = new ErrorJSONObject("API key presented in Header does not match API key presented as URL Parameter.");
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(errorObj);
				res.getOutputStream().write(json.getBytes());
			}
		}
		
		
		if (key == null) {
			for(int i = 0; i < ALLOWED_REQUEST_PATHS.length; i++) {
				if(request.getServletPath().matches(ALLOWED_REQUEST_PATHS[i])) {
					chain.doFilter(req, res); //continue
					return;
				}
			}
			
			// No Key. Don't continue. 
			ErrorJSONObject errorObj = new ErrorJSONObject("API key must be presented in order to use this API");
			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = ow.writeValueAsString(errorObj);
			res.getOutputStream().write(json.getBytes());
			
		} else {
			
			ApiKeyDTO retrievedKey = apiKeyManager.findKey(key);
			
			if (retrievedKey == null){
				// Invalid key. Don't continue. 
				ErrorJSONObject errorObj = new ErrorJSONObject("Invalid API Key");
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(errorObj);
				res.getOutputStream().write(json.getBytes());
			} else {
				try {
					apiKeyManager.logApiKeyActivity(key, requestPath);
				} catch (EntityCreationException e) {
					throw new ServletException(e);
				}
				chain.doFilter(req, res); //continue
			}
		}
	}
	
}
