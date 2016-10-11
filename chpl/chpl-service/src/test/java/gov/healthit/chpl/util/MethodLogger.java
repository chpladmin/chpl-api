package gov.healthit.chpl.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.web.controller.SearchViewController;

@Component
@Aspect
public class MethodLogger {
	private static final Logger logger = LogManager.getLogger(SearchViewController.class);
	
	@Pointcut("execution(org.springframework.cache.annotation.*)")
	public void LogCacheEvict(){
		logger.info("Executing CacheEvict annotation");
	}
	
}
