package gov.healthit.chpl.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.filter.GenericFilterBean;

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;

public class JWTAuthenticationFilter extends GenericFilterBean {

    private static final String[] ALLOWED_REQUEST_PATHS = {
            "/monitoring", "/ff4j-console"
    };

    private JWTUserConverter userConverter;

    public JWTAuthenticationFilter(JWTUserConverter userConverter) {
        this.userConverter = userConverter;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
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
        authorization = (!StringUtils.isEmpty(authorizationFromHeader) ? authorizationFromHeader
                : authorizationFromParam);

        if (authorization == null) {
            chain.doFilter(req, res); //continue
            SecurityContextHolder.getContext().setAuthentication(null);
        } else {
            User authenticatedUser;
            String jwt = null;

            try {
                jwt = authorization.split(" ")[1];
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                HttpServletResponse response = (HttpServletResponse) res;
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
            }

            if (jwt != null) {
                try {
                    authenticatedUser = userConverter.getAuthenticatedUser(jwt);
                    SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
                    chain.doFilter(req, res); // continue
                    SecurityContextHolder.getContext().setAuthentication(null);
                } catch (JWTValidationException | MultipleUserAccountsException e) {
                    HttpServletResponse response = (HttpServletResponse) res;
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token");
                }
            }
        }
    }

}
