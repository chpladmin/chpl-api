package gov.healthit.chpl.registration;

import gov.healthit.chpl.dao.ApiKeyDAO;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
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
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

@Component
public class RateLimitingInterceptor extends HandlerInterceptorAdapter implements EnvironmentAware{
 
    private static final Logger LOGGER = LogManager.getLogger(RateLimitingInterceptor.class);
    
    @Autowired
    private Environment env;
    
    @Autowired
    private MessageSource messageSource;
    
    @Autowired
    private ApiKeyDAO apiKeyDao;
    
    private String timeUnit;
    
    private int limit;
 
    private Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();
    
    private List<String> whitelist = new ArrayList<String>();
    
    public RateLimitingInterceptor(){}
    
    @Override
    public void setEnvironment(Environment env) {
        LOGGER.info("setEnvironment");
        this.env = env;
        this.timeUnit = env.getProperty("rateLimitTimeUnit");
        this.limit = Integer.valueOf(env.getProperty("rateTokenLimit"));
    }
    
    /** {@inheritDoc} */
    public String getMessage(final String messageCode, final String input, final String input2) {
        return String.format(messageSource.getMessage(
                new DefaultMessageSourceResolvable(messageCode),
                LocaleContextHolder.getLocale()), input, input2);
    }
     
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String key = null;
        String clientIdParam = request.getParameter("api_key");
        String clientIdHeader = request.getHeader("API-Key");
        
        if (!StringUtils.isEmpty(clientIdParam)) {
            key = clientIdParam;
        }else if(!StringUtils.isEmpty(clientIdHeader)){
            key = clientIdHeader;
        }else{
            return false;
        }
        
        List<ApiKeyDTO> keyDtos = apiKeyDao.findAllWhitelisted();
        
        for(ApiKeyDTO dto : keyDtos){
            whitelist.add(dto.getApiKey());
        }
        
        // let non-API requests pass
        if (whitelist.contains(key)) {
            return true;
        }
        SimpleRateLimiter rateLimiter = getRateLimiter(key);
        boolean allowRequest = rateLimiter.tryAcquire();
     
        if (!allowRequest) {
            response.sendError(HttpStatus.TOO_MANY_REQUESTS.value(), getMessage("apikey.limit", String.valueOf(limit), timeUnit));
            LOGGER.info("Client with API KEY: " + key +  " went over API KEY limit of " + limit + ".");
        }
        response.addHeader("X-RateLimit-Limit", String.valueOf(limit));
        return allowRequest;
    }
     
    private SimpleRateLimiter getRateLimiter(String clientId) {
        
        if(limiters.containsKey(clientId)){
            return limiters.get(clientId);
        }else{
            SimpleRateLimiter srl = new SimpleRateLimiter(limit, parseTimeUnit(timeUnit));
            limiters.put(clientId, srl);
            return srl;
        }
    }
    
    private TimeUnit parseTimeUnit(String unit){
        if(unit.equals("second")){
            return TimeUnit.SECONDS;
        }else if(unit.equals("minute")){
            return TimeUnit.MINUTES;
        }else if(unit.equals("hour")){
            return TimeUnit.HOURS;
        }
        return null;
    }
}