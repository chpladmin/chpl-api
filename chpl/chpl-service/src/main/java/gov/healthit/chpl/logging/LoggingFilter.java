package gov.healthit.chpl.logging;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.filter.GenericFilterBean;

public class LoggingFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        ThreadContext.put("tracker", UUID.randomUUID().toString());
        chain.doFilter(request, response); // continue
    }
}
