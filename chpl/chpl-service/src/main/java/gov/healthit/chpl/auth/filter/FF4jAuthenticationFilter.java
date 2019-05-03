package gov.healthit.chpl.auth.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import gov.healthit.chpl.auth.authentication.JWTUserConverter;
import gov.healthit.chpl.auth.user.User;

public class FF4jAuthenticationFilter extends GenericFilterBean {

    private static final String FF4J_CONSOLE_PATH = "/ff4j-console";

    private JWTUserConverter userConverter;

    // public JWTAuthenticationFilter(JWTUserConverter userConverter) {
    // this.userConverter = userConverter;
    // }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        if (request.getServletPath().contains(FF4J_CONSOLE_PATH)) {
            // Get the User...
            try {
                String authorizationFromHeader = request.getHeader("Authorization");
                String authorizationFromParam = request.getParameter("authorization");
                String authorization = (!StringUtils.isEmpty(authorizationFromHeader) ? authorizationFromHeader
                        : authorizationFromParam);
                String jwt = authorization.split(" ")[1];
                User authenticatedUser = userConverter.getAuthenticatedUser(jwt);

                chain.doFilter(req, res); // continue
                return;

            } catch (Exception e) {
                // Return a 401
            }
        } else {
            chain.doFilter(req, res); // continue
            return;
        }
    }

}
