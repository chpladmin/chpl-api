package gov.healthit.chpl.filter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import gov.healthit.chpl.api.ApiKeyManager;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApi;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsage;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsageDao;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseFieldApi;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseFieldApiUsage;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseFieldApiUsageDao;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.ApiKeyUtil;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeprecatedEndpointUsageFilter extends GenericFilterBean {
    public static final String[] IGNORED_REQUEST_PATHS = {
            "/api-docs", "/monitoring", "/ff4j-console"
    };

    private ApiKeyManager apiKeyManager;
    private DeprecatedApiUsageDao deprecatedApiUsageDao;
    private DeprecatedResponseFieldApiUsageDao deprecatedResponseFieldApiUsageDao;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private UrlPathHelper urlPathHelper;

    @Autowired
    public DeprecatedEndpointUsageFilter(ApiKeyManager apiKeyManager,
            DeprecatedApiUsageDao deprecatedApiUsageDao,
            DeprecatedResponseFieldApiUsageDao deprecatedResponseFieldApiUsageDao,
            RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.apiKeyManager = apiKeyManager;
        this.deprecatedApiUsageDao = deprecatedApiUsageDao;
        this.deprecatedResponseFieldApiUsageDao = deprecatedResponseFieldApiUsageDao;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.urlPathHelper = new UrlPathHelper();
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

        urlPathHelper.resolveAndCacheLookupPath(request);
        RequestMappingInfo requestMapping = getRequestMappingForHttpServletRequest(request);
        if (requestMapping != null) {
            HandlerMethod handlerMethod = requestMappingHandlerMapping.getHandlerMethods().get(requestMapping);
            if (isHandlerMethodDeprecated(handlerMethod)) {
                logDeprecatedApiUsage(requestMapping, handlerMethod, request);
            } else if (isHandlerResponseDeprecated(handlerMethod)) {
                logDeprecatedResponseFieldApiUsage(requestMapping, handlerMethod, request);
            }
        }

        chain.doFilter(req, res);
    }

    private void logDeprecatedApiUsage(RequestMappingInfo requestMapping, HandlerMethod handlerMethod, HttpServletRequest request) {
        Set<String> matchingUrlPatterns = requestMapping.getPatternsCondition().getPatterns();
        if (matchingUrlPatterns != null && matchingUrlPatterns.size() > 0) {
            String matchingUrlPattern = matchingUrlPatterns.iterator().next();
            LOGGER.warn(request.getRequestURI() + " maps to deprecated endpoint " + matchingUrlPattern + ", handler: " + handlerMethod);
            ApiKey apiKey = getApiKey(request);
            DeprecatedApi deprecatedApi = getDeprecatedApi(request, matchingUrlPattern);
            if (apiKey != null && deprecatedApi != null) {
                DeprecatedApiUsage deprecatedApiUsage = DeprecatedApiUsage.builder()
                        .apiKey(apiKey)
                        .api(deprecatedApi)
                        .build();
                deprecatedApiUsageDao.createOrUpdateDeprecatedApiUsage(deprecatedApiUsage);
            }
        } else {
            LOGGER.error("Could not determine unique matching URL Pattern for " + request.getMethod()
                + " Request: " + request.getRequestURI());
        }
    }

    private void logDeprecatedResponseFieldApiUsage(RequestMappingInfo requestMapping, HandlerMethod handlerMethod, HttpServletRequest request) {
        Set<String> matchingUrlPatterns = requestMapping.getPatternsCondition().getPatterns();
        if (matchingUrlPatterns != null && matchingUrlPatterns.size() > 0) {
            String matchingUrlPattern = matchingUrlPatterns.iterator().next();
            LOGGER.debug(request.getRequestURI() + " maps to endpoint with deprecated response fields " + matchingUrlPattern + ", handler: " + handlerMethod);
            ApiKey apiKey = getApiKey(request);
            DeprecatedResponseFieldApi deprecatedResponseFieldApi = getDeprecatedResponseFieldApi(request, matchingUrlPattern);
            if (apiKey != null && deprecatedResponseFieldApi != null) {
                DeprecatedResponseFieldApiUsage deprecatedResponseFieldApiUsage = DeprecatedResponseFieldApiUsage.builder()
                        .apiKey(apiKey)
                        .api(deprecatedResponseFieldApi)
                        .build();
                deprecatedResponseFieldApiUsageDao.createOrUpdateUsage(deprecatedResponseFieldApiUsage);
            }
        } else {
            LOGGER.error("Could not determine unique matching URL Pattern for " + request.getMethod()
                + " Request: " + request.getRequestURI());
        }
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
            matchingHandlers.stream().forEach(handler -> LOGGER.debug(handler));
            return null;
        }
        return matchingHandlers.get(0);
    }

    private boolean isHandlerMethodDeprecated(HandlerMethod handlerMethod) {
        return handlerMethod != null && handlerMethod.getMethodAnnotation(Deprecated.class) != null;
    }

    private boolean isHandlerResponseDeprecated(HandlerMethod handlerMethod) {
        return handlerMethod != null && (handlerMethod.getMethodAnnotation(Deprecated.class) == null)
                && (handlerMethod.getMethodAnnotation(DeprecatedApiResponseFields.class) != null);
    }

    private ApiKey getApiKey(HttpServletRequest request) {
        String key = null;
        try {
            key = ApiKeyUtil.getApiKeyFromRequest(request);
        } catch (InvalidArgumentsException ex) {
        }

        ApiKey apiKey = null;
        if (!StringUtils.isEmpty(key)) {
            try {
                apiKey = apiKeyManager.findKey(key);
            } catch (EntityRetrievalException ex) {
                LOGGER.error("Could not find API Key with value '" + key + "'.");
            }
        }
        return apiKey;
    }

    private DeprecatedApi getDeprecatedApi(HttpServletRequest request, String matchingUrlPattern) {
        HttpMethod method = null;
        try {
            method = HttpMethod.valueOf(request.getMethod());
        } catch (Exception ex) {
            LOGGER.error("No HttpMethod found with value '" + request.getMethod() + "'.");
            return null;
        }

        DeprecatedApi deprecatedApi = deprecatedApiUsageDao.getDeprecatedApi(method, matchingUrlPattern, null);
        if (deprecatedApi == null) {
            LOGGER.error("No deprecated API was found matching request method '" + method.name() + "', url pattern '" + matchingUrlPattern + "'.");
        }
        return deprecatedApi;
    }

    private DeprecatedResponseFieldApi getDeprecatedResponseFieldApi(HttpServletRequest request, String matchingUrlPattern) {
        HttpMethod method = null;
        try {
            method = HttpMethod.valueOf(request.getMethod());
        } catch (Exception ex) {
            LOGGER.error("No HttpMethod found with value '" + request.getMethod() + "'.");
            return null;
        }

        DeprecatedResponseFieldApi deprecatedApi = deprecatedResponseFieldApiUsageDao.getDeprecatedApi(method, matchingUrlPattern);
        if (deprecatedApi == null) {
            LOGGER.error("No deprecated response field API was found matching request method '" + method.name() + "', url pattern '" + matchingUrlPattern + "'.");
        }
        return deprecatedApi;
    }
}
