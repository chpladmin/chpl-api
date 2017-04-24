package gov.healthit.chpl.web.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.CacheStatus;
import gov.healthit.chpl.caching.CacheUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

@Api
@RestController
public class StatusController {
	private static final Logger logger = LogManager.getLogger(StatusController.class);
	@Autowired private CacheUtil cacheUtil;

	@ApiOperation(value="Check that the rest services are up and running.", 
			notes="")
	@RequestMapping(value="/status", method=RequestMethod.GET,
			produces="application/json; charset=utf-8")
	public @ResponseBody String getStatus() {
		logger.warn("/status called");
		return "{\"status\": \"OK\"}";
	}
	
	@ApiOperation(value = "Check if the basic search cache has completed loading. "
			+ "{ status: 'OK' } is returned if it's finished and { status: 'INITIALIZING' } is returned if not.",
			notes="")
	@RequestMapping(value="/cache_status", method=RequestMethod.GET, produces="application/json; charset=utf-8")
	public @ResponseBody String getCacheStatus() {
		CacheManager manager = cacheUtil.getMyCacheManager();
		Cache basicCache = manager.getCache(CacheNames.BASIC_SEARCH);
		if(basicCache == null || basicCache.getSize() == 0) {
			return "{\"status\": \"" + CacheStatus.INITIALIZING + "\"}";
		}
		return "{\"status\": \"" + CacheStatus.OK + "\"}";
	}
}
