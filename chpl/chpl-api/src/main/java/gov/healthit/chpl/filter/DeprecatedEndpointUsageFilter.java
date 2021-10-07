package gov.healthit.chpl.filter;

import java.io.IOException;
import java.util.LinkedHashSet;
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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import gov.healthit.chpl.api.ApiKeyManager;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApi;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsage;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsageDao;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.ApiKeyUtil;
import gov.healthit.chpl.util.DeprecatedFieldExplorer;
import gov.healthit.chpl.web.controller.annotation.DeprecatedResponseFields;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class DeprecatedEndpointUsageFilter extends GenericFilterBean {
    public static final String[] IGNORED_REQUEST_PATHS = {
            "/api-docs", "/monitoring", "/ff4j-console"
    };

    private ApiKeyManager apiKeyManager;
    private DeprecatedApiUsageDao deprecatedApiUsageDao;
    private DeprecatedFieldExplorer deprecatedFieldExplorer;
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    @Autowired
    public DeprecatedEndpointUsageFilter(ApiKeyManager apiKeyManager,
            DeprecatedApiUsageDao deprecatedApiUsageDao,
            RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.apiKeyManager = apiKeyManager;
        this.deprecatedApiUsageDao = deprecatedApiUsageDao;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.deprecatedFieldExplorer = new DeprecatedFieldExplorer();
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
                Set<String> matchingUrlPatterns = requestMapping.getPatternsCondition().getPatterns();
                if (matchingUrlPatterns != null && matchingUrlPatterns.size() > 0) {
                    String matchingUrlPattern = matchingUrlPatterns.iterator().next();
                    LOGGER.warn(request.getRequestURI() + " maps to deprecated endpoint " + matchingUrlPattern + ", handler: " + handlerMethod);
                    ApiKey apiKey = getApiKey(request);
                    DeprecatedApi deprecatedApi = getApi(request, matchingUrlPattern);
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
            } else if (isHandlerReturnTypeDeprecated(handlerMethod)) {
                String className = handlerMethod.getMethodAnnotation(DeprecatedResponseFields.class).responseClass().getName();
                LOGGER.debug("Finding all deprecated fields for class " + className);
                Set<String> deprecatedFieldNames = new LinkedHashSet<String>();
                deprecatedFieldExplorer.getAllDeprecatedFields(
                        handlerMethod.getMethodAnnotation(DeprecatedResponseFields.class).responseClass(),
                        deprecatedFieldNames, "");
                if (CollectionUtils.isEmpty(deprecatedFieldNames)) {
                    LOGGER.debug("No deprecated fields found for class " + className);
                }
                deprecatedFieldNames.stream()
                    .forEach(df -> System.out.println(df));
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
            matchingHandlers.stream().forEach(handler -> LOGGER.debug(handler));
            return null;
        }
        return matchingHandlers.get(0);
    }

    private boolean isHandlerMethodDeprecated(HandlerMethod handlerMethod) {
        return handlerMethod != null && handlerMethod.getMethodAnnotation(Deprecated.class) != null;
    }

    private boolean isHandlerReturnTypeDeprecated(HandlerMethod handlerMethod) {
        return handlerMethod != null && (handlerMethod.getMethodAnnotation(Deprecated.class) == null)
                && (handlerMethod.getMethodAnnotation(DeprecatedResponseFields.class) != null);
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

    private DeprecatedApi getApi(HttpServletRequest request, String matchingUrlPattern) {
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
}
