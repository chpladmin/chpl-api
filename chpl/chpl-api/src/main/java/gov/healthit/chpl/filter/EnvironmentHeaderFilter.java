package gov.healthit.chpl.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class EnvironmentHeaderFilter extends OncePerRequestFilter {

    private String serverEnvironment;

    @Autowired
    public EnvironmentHeaderFilter(@Value("${server.environment}") String serverEnvironment) {
        this.serverEnvironment = serverEnvironment != null ? serverEnvironment : "";
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (serverEnvironment.equalsIgnoreCase("production")) {
            response.addHeader("Environment", "PRODUCTION");
        } else {
            response.addHeader("Environment", "NON-PRODUCTION");
        }

        filterChain.doFilter(request, response);
    }
}
