package gov.healthit.chpl.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import gov.healthit.chpl.util.ServerEnvironment;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class EnvironmentHeaderFilter extends OncePerRequestFilter {

    private ServerEnvironment serverEnvironment;

    @Autowired
    public EnvironmentHeaderFilter(@Value("${server.environment}") String serverEnvironment) {
        this.serverEnvironment = serverEnvironment != null ? ServerEnvironment.getByName(serverEnvironment) : null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (serverEnvironment.equals(ServerEnvironment.PRODUCTION)) {
            response.addHeader("Environment", "PRODUCTION");
        } else {
            response.addHeader("Environment", "NON-PRODUCTION");
        }

        filterChain.doFilter(request, response);
    }
}
