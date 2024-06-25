package gov.healthit.chpl.filter;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import gov.healthit.chpl.api.ApiKeyManager;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.domain.error.ErrorResponse;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.ApiKeyUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class APIKeyAuthenticationFilter extends GenericFilterBean {
    public static final String[] ALLOWED_REQUEST_PATHS = {
            "/v3/api-docs", "/system-status", "/status", "/cache_status", "/monitoring", "/ff4j-console"
    };

    private ApiKeyManager apiKeyManager;

    public APIKeyAuthenticationFilter(ApiKeyManager apiKeyManager) {
        this.apiKeyManager = apiKeyManager;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = null;
        HttpServletResponse response = (HttpServletResponse) res;

        if (req instanceof jakarta.servlet.http.HttpServletRequest) {
            request = (HttpServletRequest) req;
        } else {
            throw new ServletException("Request was not correct type");
        }

        for (int i = 0; i < ALLOWED_REQUEST_PATHS.length; i++) {
            if (request.getServletPath().matches(ALLOWED_REQUEST_PATHS[i])) {
                chain.doFilter(req, res); // continue
                return;
            }
        }

        String requestPath;
        if (request.getQueryString() == null) {
            requestPath = request.getRequestURI();
        } else {
            requestPath = request.getRequestURI() + "?" + request.getQueryString();
        }

        String requestMethod = request.getMethod();
        String key = null;
        try {
            key = ApiKeyUtil.getApiKeyFromRequest(request);
        } catch (InvalidArgumentsException ex) {
            // Keys don't match. Don't continue.
            ErrorResponse errorObj = new ErrorResponse(
                    "API key presented in Header does not match API key presented as URL Parameter.");
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(errorObj);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, json);
            return;
        }

        if (StringUtils.isEmpty(key)) {
            // No Key. Don't continue.
            ErrorResponse errorObj = new ErrorResponse("API key must be presented in order to use this API");
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(errorObj);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, json);
            return;
        } else {
            try {
                ApiKey retrievedKey = apiKeyManager.findKey(key);
                if (retrievedKey == null) {
                    // Invalid key. Don't continue.
                    ErrorResponse errorObj = new ErrorResponse("Invalid API Key");
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String json = ow.writeValueAsString(errorObj);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, json);
                    return;
                } else {
                    try {
                        apiKeyManager.logApiKeyActivity(key, requestPath, requestMethod);
                    } catch (EntityCreationException e) {
                        throw new ServletException(e);
                    }
                    chain.doFilter(req, res); // continue
                }
            } catch (EntityRetrievalException ex) {
                LOGGER.error("Cannot find key for HTTP filter: " + key);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid API Key");
                return;
            }
        }
    }

}
