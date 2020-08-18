package gov.healthit.chpl.registration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import gov.healthit.chpl.dao.ApiKeyDAO;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

/**
 * Interceptor that handles rate limiting of API-Keys.
 * @author blindsey
 *
 */
@Component
public class RateLimitingInterceptor extends HandlerInterceptorAdapter implements EnvironmentAware {

    private static final Logger LOGGER = LogManager.getLogger(RateLimitingInterceptor.class);

    @Autowired
    private Environment env;

    @Autowired
    private ApiKeyDAO apiKeyDao;

    @Autowired
    private ErrorMessageUtil errorUtil;

    private String timeUnit;

    private int limit;

    private Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();

    private List<String> unrestrictedApiKeys = new ArrayList<String>();

    /** Default constructor. */
    public RateLimitingInterceptor() {
    }

    @Override
    public void setEnvironment(final Environment environment) {
        LOGGER.info("setEnvironment");
        this.env = environment;
        this.timeUnit = this.env.getProperty("rateLimitTimeUnit");
        this.limit = Integer.parseInt(this.env.getProperty("rateTokenLimit"));
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
            final HttpServletResponse response, final Object handler) throws Exception {
        String key = null;
        String clientIdParam = request.getParameter("api_key");
        String clientIdHeader = request.getHeader("API-Key");

        if (!StringUtils.isEmpty(clientIdParam)) {
            key = clientIdParam;
        } else if (!StringUtils.isEmpty(clientIdHeader)) {
            key = clientIdHeader;
        } else {
            return false;
        }

        List<ApiKeyDTO> keyDtos = apiKeyDao.findAllUnrestricted();

        for (ApiKeyDTO dto : keyDtos) {
            unrestrictedApiKeys.add(dto.getApiKey());
        }

        // let non-API requests pass
        if (unrestrictedApiKeys.contains(key)) {
            return true;
        }
        SimpleRateLimiter rateLimiter = getRateLimiter(key);
        boolean allowRequest = rateLimiter.tryAcquire();

        if (!allowRequest) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                    errorUtil.getMessage("apikey.limit", String.valueOf(limit), timeUnit));
            LOGGER.info("Client with API KEY: " + key + " went over API KEY limit of " + limit + ".");
        }
        response.addHeader("X-RateLimit-Limit", String.valueOf(limit));
        return allowRequest;
    }

    private SimpleRateLimiter getRateLimiter(final String clientId) {

        if (limiters.containsKey(clientId)) {
            return limiters.get(clientId);
        } else {
            SimpleRateLimiter srl = new SimpleRateLimiter(limit, parseTimeUnit(timeUnit));
            limiters.put(clientId, srl);
            return srl;
        }
    }

    private TimeUnit parseTimeUnit(final String unit) {
        if (unit.equals("second")) {
            return TimeUnit.SECONDS;
        } else if (unit.equals("minute")) {
            return TimeUnit.MINUTES;
        } else if (unit.equals("hour")) {
            return TimeUnit.HOURS;
        }
        return null;
    }
}
