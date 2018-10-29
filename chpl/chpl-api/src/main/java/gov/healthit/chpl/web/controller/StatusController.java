package gov.healthit.chpl.web.controller;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.caching.CacheStatus;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * Controller for checking status of the system.
 * @author alarned
 *
 */
@Api
@RestController
public class StatusController {
    private static final Logger LOGGER = LogManager.getLogger(StatusController.class);

    /**
     * Get the status, indicating if the server is running at all.
     * @return JSON value that indicates the server is running
     */
    @ApiOperation(value = "Check that the rest services are up and running.", notes = "")
    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody String getStatus() {
        LOGGER.warn("/status called");
        return "{\"status\": \"OK\"}";
    }

    /**
     * Get information about the basic search cache, including whether it's completed loading and its "age".
     * @return JSON object with status of load and "age"
     */
    @ApiOperation(
            value = "Check if the basic search cache has completed loading. "
                    + "{ status: 'OK', age: long } is returned if it's finished and "
                    + "{ status: 'INITIALIZING' } is returned if not. "
                    + "Age indicates the number of miliseconds since the cache "
                    + "was last refreshed.",
                    notes = "")
    @RequestMapping(value = "/cache_status", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody String getCacheStatus() {
        CacheManager manager = CacheManager.getInstance();
        Cache basicCache = manager.getCache(CacheNames.COLLECTIONS_LISTINGS);
        if (basicCache == null || basicCache.getKeysNoDuplicateCheck().size() == 0) {
            return "{\"status\": \"" + CacheStatus.INITIALIZING + "\"}";
        }
        if (basicCache.get("CACHE_GENERATION_TIME") != null) {
            long age = (new Date()).getTime() - (long) basicCache.get("CACHE_GENERATION_TIME").getObjectValue();
            return "{\"status\": \"" + CacheStatus.OK + "\",\"age\": " + age + "}";
        } else {
            return "{\"status\": \"" + CacheStatus.OK + "\",\"age\": -1}";
        }
    }
}
