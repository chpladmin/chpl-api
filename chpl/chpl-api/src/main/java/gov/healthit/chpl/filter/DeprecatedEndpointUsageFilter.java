package gov.healthit.chpl.filter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.LinkedHashMap;
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
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import gov.healthit.chpl.api.ApiKeyManager;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsage;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedApiUsageDao;
import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.ApiKeyUtil;
import gov.healthit.chpl.util.DeprecatedResponseFieldExplorer;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
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
    private RequestMappingHandlerMapping requestMappingHandlerMapping;
    private UrlPathHelper urlPathHelper;
    private DeprecatedResponseFieldExplorer deprecatedFieldExplorer;

    @Autowired
    public DeprecatedEndpointUsageFilter(ApiKeyManager apiKeyManager,
            DeprecatedApiUsageDao deprecatedApiUsageDao,
            RequestMappingHandlerMapping requestMappingHandlerMapping) {
        this.apiKeyManager = apiKeyManager;
        this.deprecatedApiUsageDao = deprecatedApiUsageDao;
        this.requestMappingHandlerMapping = requestMappingHandlerMapping;
        this.deprecatedFieldExplorer = new DeprecatedResponseFieldExplorer();
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
                logDeprecatedResponseFieldsApiUsage(requestMapping, handlerMethod, request);
            } else if (handlerMethod == null) {
                LOGGER.error("No handler method was found for request " + request.getRequestURI());
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
            if (apiKey != null) {
                DeprecatedApi deprecatedApiAnnotation = handlerMethod.getMethodAnnotation(DeprecatedApi.class);
                DeprecatedApiUsage deprecatedApiUsage = DeprecatedApiUsage.builder()
                        .apiKey(apiKey)
                        .httpMethod(deprecatedApiAnnotation.httpMethod())
                        .apiOperation(deprecatedApiAnnotation.friendlyUrl())
                        .message(deprecatedApiAnnotation.message())
                        .removalDate(LocalDate.parse(deprecatedApiAnnotation.removalDate()))
                        .responseField(null)
                        .build();
                deprecatedApiUsageDao.createOrUpdateDeprecatedApiUsage(deprecatedApiUsage);
            }
        } else {
            LOGGER.error("Could not determine unique matching URL Pattern for " + request.getMethod()
                + " Request: " + request.getRequestURI());
        }
    }

    private void logDeprecatedResponseFieldsApiUsage(RequestMappingInfo requestMapping, HandlerMethod handlerMethod, HttpServletRequest request) {
        Set<String> matchingUrlPatterns = requestMapping.getPatternsCondition().getPatterns();
        if (matchingUrlPatterns != null && matchingUrlPatterns.size() > 0) {
            String matchingUrlPattern = matchingUrlPatterns.iterator().next();
            LOGGER.debug(request.getRequestURI() + " maps to endpoint with deprecated response fields " + matchingUrlPattern + ", handler: " + handlerMethod);
            ApiKey apiKey = getApiKey(request);
            if (apiKey != null) {
                DeprecatedApiResponseFields deprecatedApiResponseFieldsAnnotation = handlerMethod.getMethodAnnotation(DeprecatedApiResponseFields.class);
                Class classWithDeprecatedResponseFields = deprecatedApiResponseFieldsAnnotation.responseClass();
                //map with keys as the field name and values as the Field or Method
                LinkedHashMap<String, Object> deprecatedItems = deprecatedFieldExplorer.getUniqueDeprecatedItemsForClass(classWithDeprecatedResponseFields);

                deprecatedItems.keySet().stream()
                    .forEach(deprecatedFieldName -> logDeprecatedResponseFieldApiUsage(apiKey,
                            deprecatedApiResponseFieldsAnnotation,
                            deprecatedFieldName,
                            deprecatedItems.get(deprecatedFieldName)));
            }
        } else {
            LOGGER.error("Could not determine unique matching URL Pattern for " + request.getMethod()
                + " Request: " + request.getRequestURI());
        }
    }

    private void logDeprecatedResponseFieldApiUsage(ApiKey apiKey, DeprecatedApiResponseFields deprecatedApiAnnotaiton,
            String deprecatedFieldName, Object classItem) {
        DeprecatedResponseField deprecatedResponseFieldAnnotation = null;
        if (classItem instanceof Field) {
            Field field = (Field) classItem;
            deprecatedResponseFieldAnnotation = field.getAnnotation(DeprecatedResponseField.class);
        } else if (classItem instanceof Method) {
            Method method = (Method) classItem;
            deprecatedResponseFieldAnnotation = method.getAnnotation(DeprecatedResponseField.class);
        }
        if (deprecatedResponseFieldAnnotation == null) {
            LOGGER.warn("Expected a DeprecatedResponseField annotation on " + classItem + " but it was not found.");
        } else {
            DeprecatedApiUsage deprecatedApiUsage = DeprecatedApiUsage.builder()
                    .apiKey(apiKey)
                    .httpMethod(deprecatedApiAnnotaiton.httpMethod())
                    .apiOperation(deprecatedApiAnnotaiton.friendlyUrl())
                    .message(deprecatedResponseFieldAnnotation.message())
                    .removalDate(LocalDate.parse(deprecatedResponseFieldAnnotation.removalDate()))
                    .responseField(deprecatedFieldName)
                    .build();
            deprecatedApiUsageDao.createOrUpdateDeprecatedApiUsage(deprecatedApiUsage);
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
        return handlerMethod != null && handlerMethod.getMethodAnnotation(DeprecatedApi.class) != null;
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
}
