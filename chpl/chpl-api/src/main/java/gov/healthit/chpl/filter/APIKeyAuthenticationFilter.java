package gov.healthit.chpl.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import gov.healthit.chpl.api.ApiKeyManager;
import gov.healthit.chpl.api.domain.ApiKeyDTO;
import gov.healthit.chpl.domain.error.ErrorResponse;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class APIKeyAuthenticationFilter extends GenericFilterBean {
    public static final String[] ALLOWED_REQUEST_PATHS = {
            "/api-docs", "/system-status", "/status", "/cache_status", "/monitoring", "/ff4j-console"
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

        if (req instanceof javax.servlet.http.HttpServletRequest) {
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
        String keyFromHeader = request.getHeader("API-Key");
        String keyFromParam = request.getParameter("api_key");

        if (keyFromHeader != null && keyFromHeader.equals(keyFromParam)) {
            key = keyFromHeader;
        } else {
            if (keyFromHeader == null) {
                key = keyFromParam;
            } else if (keyFromParam == null) {
                key = keyFromHeader;
            } else {
                // Keys don't match. Don't continue.
                ErrorResponse errorObj = new ErrorResponse(
                        "API key presented in Header does not match API key presented as URL Parameter.");
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(errorObj);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, json);
                return;
            }
        }

        if (key == null) {
            // No Key. Don't continue.
            ErrorResponse errorObj = new ErrorResponse("API key must be presented in order to use this API");
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(errorObj);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, json);
            return;
        } else {
            try {
                ApiKeyDTO retrievedKey = apiKeyManager.findKey(key);

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
