package gov.healthit.chpl.auth.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.json.ErrorJSONObject;
import gov.healthit.chpl.auth.jwt.JWTValidationException;
import gov.healthit.chpl.auth.user.User;

public class JWTAuthenticationFilter extends GenericFilterBean {

    private static final String[] ALLOWED_REQUEST_PATHS = { "/monitoring" };

    private JWTUserConverter userConverter;

    public JWTAuthenticationFilter(JWTUserConverter userConverter){
        this.userConverter = userConverter;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        for (int i = 0; i < ALLOWED_REQUEST_PATHS.length; i++) {
            if (request.getServletPath().matches(ALLOWED_REQUEST_PATHS[i])) {
                chain.doFilter(req, res); // continue
                return;
            }
        }

        String authorization = null;
        String authorizationFromHeader = request.getHeader("Authorization");
        String authorizationFromParam = request.getParameter("authorization");
        authorization = (!StringUtils.isEmpty(authorizationFromHeader) ? authorizationFromHeader : authorizationFromParam);

        if (authorization == null){
            chain.doFilter(req, res); //continue
            SecurityContextHolder.getContext().setAuthentication(null);
        } else {
            User authenticatedUser;
            String jwt = null;

            try { 
                jwt = authorization.split(" ")[1];
            } catch (java.lang.ArrayIndexOutOfBoundsException e){
                ErrorJSONObject errorObj = new ErrorJSONObject("Token must be presented in the form: Bearer token");
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(errorObj);
                res.getOutputStream().write(json.getBytes());
            }

            if (jwt != null){
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
}
