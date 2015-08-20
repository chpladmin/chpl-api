package gov.healthit.chpl.auth.filter;

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.json.ErrorJSONObject;
import gov.healthit.chpl.auth.jwt.JWTValidationException;
import gov.healthit.chpl.auth.user.User;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JWTAuthenticationFilter extends GenericFilterBean {
	
	
	private JWTUserConverter userConverter;
	
	public JWTAuthenticationFilter(JWTUserConverter userConverter){
		this.userConverter = userConverter;
	}
	

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) req;
		
		String authorizationHeader = request.getHeader("Authorization");
		
		if (authorizationHeader == null){
			chain.doFilter(req, res); //continue
			SecurityContextHolder.getContext().setAuthentication(null);
		} else {
			User authenticatedUser;
			String jwt = authorizationHeader.split(" ")[1];
			try {
				authenticatedUser = userConverter.getAuthenticatedUser(jwt);
				SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
				chain.doFilter(req, res); //continue
				SecurityContextHolder.getContext().setAuthentication(null);
				
			} catch (JWTValidationException e) {
				
				ErrorJSONObject errorObj = new ErrorJSONObject(e.getMessage());
				ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
				String json = ow.writeValueAsString(errorObj);
				res.getOutputStream().write(json.getBytes());
				
			}
		}
	}
}
