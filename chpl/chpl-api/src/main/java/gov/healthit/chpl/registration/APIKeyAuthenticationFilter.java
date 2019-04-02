package gov.healthit.chpl.registration;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.filter.GenericFilterBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import gov.healthit.chpl.auth.json.ErrorJSONObject;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ApiKeyManager;

/**
 * Filter used to ensure calls to API that require an API-Key have a valid API-Key.
 */
public class APIKeyAuthenticationFilter extends GenericFilterBean {
    private static final Logger LOGGER = LogManager.getLogger(APIKeyAuthenticationFilter.class);
    /**
     * Requests that do not require an API Key.
     */
    public static final String[] ALLOWED_REQUEST_PATHS = {
            "/api-docs", "/system-status", "/status", "/cache_status", "/monitoring"
    };

    private ApiKeyManager apiKeyManager;

    /**
     * Constructor with key manager.
     * @param apiKeyManager the api key manager
     */
    public APIKeyAuthenticationFilter(final ApiKeyManager apiKeyManager) {
        this.apiKeyManager = apiKeyManager;
    }

    @Override
    public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = null;
        if (req instanceof javax.servlet.http.HttpServletRequest) {
            request = (HttpServletRequest) req;
        } else {
            throw new ServletException("Request was not correct type");
        }
        String requestPath;
        if (request.getQueryString() == null) {
            requestPath = request.getRequestURI();
        } else {
            requestPath = request.getRequestURI() + "?" + request.getQueryString();
        }

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
                ErrorJSONObject errorObj = new ErrorJSONObject(
                        "API key presented in Header does not match API key presented as URL Parameter.");
                ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                String json = ow.writeValueAsString(errorObj);
                res.getOutputStream().write(json.getBytes("UTF-8"));
            }
        }

        if (key == null) {
            for (int i = 0; i < ALLOWED_REQUEST_PATHS.length; i++) {
                if (request.getServletPath().matches(ALLOWED_REQUEST_PATHS[i])) {
                    chain.doFilter(req, res); // continue
                    return;
                }
            }

            // No Key. Don't continue.
            ErrorJSONObject errorObj = new ErrorJSONObject("API key must be presented in order to use this API");
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            String json = ow.writeValueAsString(errorObj);
            res.getOutputStream().write(json.getBytes("UTF-8"));
        } else {
            try {
                ApiKeyDTO retrievedKey = apiKeyManager.findKey(key);

                if (retrievedKey == null) {
                    // Invalid key. Don't continue.
                    ErrorJSONObject errorObj = new ErrorJSONObject("Invalid API Key");
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String json = ow.writeValueAsString(errorObj);
                    res.getOutputStream().write(json.getBytes("UTF-8"));
                } else {
                    try {
                        apiKeyManager.logApiKeyActivity(key, requestPath);
                    } catch (final EntityCreationException e) {
                        throw new ServletException(e);
                    }
                    chain.doFilter(req, res); // continue
                }
            } catch (final EntityRetrievalException ex) {
                LOGGER.error("Cannot find key for HTTP filter: " + key);
            }
        }
    }

}
