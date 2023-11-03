package gov.healthit.chpl.ratelimiting;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import gov.healthit.chpl.api.dao.ApiKeyDAO;
import gov.healthit.chpl.util.ErrorMessageUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class RateLimitingInterceptor implements HandlerInterceptor {

    private Integer rateLimitRequestCount;
    private Integer rateLimitTimePeriod;
    private ErrorMessageUtil errorUtil;

    private Map<String, Bucket> rateLimitingBuckets = new HashMap<String, Bucket>();
    private List<String> unrestrictedApiKeys = new ArrayList<String>();

    public RateLimitingInterceptor(ApiKeyDAO apiKeyDAO, ErrorMessageUtil errorUtil, Integer rateLimitRequestCount, Integer rateLimitTimePeriod) {
        apiKeyDAO.findAllUnrestricted().forEach(apiKey -> unrestrictedApiKeys.add(apiKey.getKey()));

        this.errorUtil = errorUtil;
        this.rateLimitRequestCount = rateLimitRequestCount;
        this.rateLimitTimePeriod = rateLimitTimePeriod;
    }

    @Override
    public boolean preHandle(final HttpServletRequest request,
            final HttpServletResponse response, final Object handler) throws Exception {

        String apiKey = getRequestApiKey(request);
        if (apiKey == null) {
            return false;
        }

        // let non-API requests pass
        if (unrestrictedApiKeys.contains(apiKey)) {
            return true;
        }

        Boolean allowRequest = getBucketForApiKey(apiKey).tryConsume(1);

        if (!allowRequest) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                    errorUtil.getMessage("apikey.limit", rateLimitRequestCount, rateLimitTimePeriod));
            LOGGER.info("Client with API KEY: {} went over API KEY limit of {} per {} second(s).", apiKey, rateLimitRequestCount, rateLimitTimePeriod);
        }

        response.addHeader("X-RateLimit-Limit", rateLimitRequestCount +" call(s) every " + rateLimitTimePeriod + " second(s)");
        return allowRequest;
    }

    private Bucket getBucketForApiKey(String apiKey) {
        if (!rateLimitingBuckets.containsKey(apiKey)) {
            rateLimitingBuckets.put(apiKey, Bucket.builder()
                    .addLimit(Bandwidth.classic(rateLimitRequestCount,
                            Refill.intervally(rateLimitRequestCount, Duration.ofSeconds(rateLimitTimePeriod))))
            .build());
        }
        return rateLimitingBuckets.get(apiKey);
    }

    private String getRequestApiKey(HttpServletRequest request) {
        String clientIdParam = request.getParameter("api_key");
        String clientIdHeader = request.getHeader("API-Key");

        if (!StringUtils.isEmpty(clientIdParam)) {
            return clientIdParam;
        } else if (!StringUtils.isEmpty(clientIdHeader)) {
            return clientIdHeader;
        } else {
            return null;
        }
    }
}
