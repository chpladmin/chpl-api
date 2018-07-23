package gov.healthit.chpl.registration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

@Component
public class RateLimitingInterceptor extends HandlerInterceptorAdapter implements EnvironmentAware{
 
    private static final Logger logger = LogManager.getLogger(RateLimitingInterceptor.class);
    
    @Autowired
    private Environment env;
    
    private String timeUnit;
    
    private int limit;
 
    private Map<String, SimpleRateLimiter> limiters = new ConcurrentHashMap<>();
    
    public RateLimitingInterceptor(){}
    
    @Override
    public void setEnvironment(Environment env) {
        logger.info("setEnvironment");
        this.env = env;
        this.timeUnit = env.getProperty("rateLimitTimeUnit");
        this.limit = Integer.valueOf(env.getProperty("rateTokenLimit"));
    }
     
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String clientId = request.getParameter("api_key");
        
        // let non-API requests pass
        if (clientId == null) {
            return true;
        }
        SimpleRateLimiter rateLimiter = getRateLimiter(clientId);
        boolean allowRequest = rateLimiter.tryAcquire();
     
        if (!allowRequest) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            logger.info("Client with API KEY: " + clientId +  " went over API KEY limit of " + limit + ".");
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

    @PreDestroy
    public void destroy() {
        // loop and finalize all limiters
    }
}