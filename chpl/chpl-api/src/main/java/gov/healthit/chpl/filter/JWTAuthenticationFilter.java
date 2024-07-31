package gov.healthit.chpl.filter;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.springframework.web.filter.GenericFilterBean;

import gov.healthit.chpl.auth.authentication.JWTUserConverterFacade;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.exception.JWTValidationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class JWTAuthenticationFilter extends GenericFilterBean {

    private static final String[] ALLOWED_REQUEST_PATHS = {
            "/monitoring", "/ff4j-console", "/v3/api-docs", "(.*)refresh-token(.*)", "(.*)logout(.*)"
    };

    private JWTUserConverterFacade userConverterFacade;

    public JWTAuthenticationFilter(JWTUserConverterFacade userConverterFacade) {
        this.userConverterFacade = userConverterFacade;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        HttpServletRequest request = (HttpServletRequest) req;

        LOGGER.info("Handling: {}", request.getServletPath());

        for (int i = 0; i < ALLOWED_REQUEST_PATHS.length; i++) {
            if (request.getServletPath().matches(ALLOWED_REQUEST_PATHS[i])) {
                LOGGER.info("Skipping: {}", request.getServletPath());
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
            JWTAuthenticatedUser authenticatedUser;
            String jwt = null;

            try {
                jwt = authorization.split(" ")[1];
            } catch (java.lang.ArrayIndexOutOfBoundsException e) {
                sendInvalidTokenResponse((HttpServletResponse) res);
            }

            if (jwt != null) {
                try {
                    authenticatedUser = userConverterFacade.getAuthenticatedUser(jwt);
                    if (authenticatedUser != null) {
                        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);
                        chain.doFilter(req, res); // continue
                        SecurityContextHolder.getContext().setAuthentication(null);
                    } else {
                        sendInvalidTokenResponse((HttpServletResponse) res);
                    }
                } catch (JWTValidationException | MultipleUserAccountsException e) {
                    sendInvalidTokenResponse((HttpServletResponse) res);
                }
            }
        }
    }

    private void sendInvalidTokenResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().append("Invalid authentication token.");
    }
}
