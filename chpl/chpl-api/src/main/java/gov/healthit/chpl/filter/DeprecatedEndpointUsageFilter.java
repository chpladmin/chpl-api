package gov.healthit.chpl.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeprecatedEndpointUsageFilter extends GenericFilterBean {
    public static final String[] IGNORED_REQUEST_PATHS = {
            "/api-docs", "/monitoring", "/ff4j-console"
    };

    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    public DeprecatedEndpointUsageFilter(RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;

        boolean isIgnored = Stream.of(IGNORED_REQUEST_PATHS).filter(path -> path.matches(request.getServletPath())).findAny().isPresent();
        if (isIgnored) {
            chain.doFilter(req, res);
            return;
        }

        RequestMappingInfo requestMapping = getRequestMappingForHttpServletRequest(request);
        if (requestMapping != null) {
            HandlerMethod handlerMethod = requestMappingHandlerMapping.getHandlerMethods().get(requestMapping);
            if (isHandlerMethodDeprecated(handlerMethod)) {
                LOGGER.warn(request.getRequestURI() + " maps to deprecated handler: " + handlerMethod);
                //TODO: log to db
            }
        }
        chain.doFilter(req, res);
    }

    private RequestMappingInfo getRequestMappingForHttpServletRequest(HttpServletRequest request) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        List<RequestMappingInfo> matchingHandlers = handlerMethods.keySet().stream()
            .filter(key -> key.getMatchingCondition(request) != null)
            .collect(Collectors.toList());

        if (matchingHandlers == null || matchingHandlers.size() == 0) {
            LOGGER.error("No request mapping found for " + request.getMethod() + " Request: "
                    + request.getRequestURI());
            return null;
        } else if (matchingHandlers != null && matchingHandlers.size() > 1) {
            LOGGER.error("Ambiguous request mapping (" + matchingHandlers.size() + " found) for "
                    + request.getMethod() + " Request: " + request.getRequestURI());
            return null;
        }
        return matchingHandlers.get(0);
    }

    private boolean isHandlerMethodDeprecated(HandlerMethod handlerMethod) {
        return handlerMethod != null && handlerMethod.getMethodAnnotation(Deprecated.class) != null;
    }
}
