package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.caching.CacheInitializor;
import gov.healthit.chpl.domain.status.CacheStatus;
import gov.healthit.chpl.domain.status.CacheStatusName;
import gov.healthit.chpl.domain.status.SystemStatus;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.domain.status.ServerStatus;
import gov.healthit.chpl.domain.status.ServerStatusName;
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
@Loggable
public class StatusController {
    private static final Logger LOGGER = LogManager.getLogger(StatusController.class);

    /**
     * Get the current status of the system. Response indicates whether the system is up
     * and whether the caches are initializing or complete.
     * @return JSON value that indicates the server is running
     */
    @ApiOperation(
            value = "Check that the rest services are up and running and indicate whether "
                    + "the pre-loaded caches are initializing or have completed."
                    + "{\"running\":\"OK\", \"cache\":\"OK\"} is returned if all is well."
                    + "If the cache is still initializing, the returned value will be "
                    + "{\"running\":\"OK\", \"cache\":\"INITIALIZING\"}.",
                    notes = "")
    @RequestMapping(value = "/system-status", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody SystemStatus getCombinedStatus() {
        SystemStatus response = new SystemStatus();
        //if this code is running then the server is up.
        response.setRunning(ServerStatusName.OK.name());
        //calculate the cache status
        response.setCache(determineCacheStatus().name());
        return response;
    }

    /**
     * Get the status, indicating if the server is running at all.
     * @return JSON value that indicates the server is running
     */
    @Deprecated
    @ApiOperation(
            value = "DEPRECATED. Use /system-status instead. "
                    + "Check that the rest services are up and running."
                    + "{\"status\":\"OK\"} is returned if all is well.",
                    notes = "")
    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ServerStatus getStatus() {
        LOGGER.warn("/status called");
        ServerStatus response = new ServerStatus();
        response.setStatus(ServerStatusName.OK.name());
        return response;
    }

    /**
     * Get information about the basic search cache, including whether it's completed loading and its "age".
     * @return JSON object with status of load and "age"
     */
    @Deprecated
    @ApiOperation(
            value = "DEPRECATED. Use /system-status instead. "
                    + "Check the status of every cache. "
                    + "{\"status\":\"OK\"} is returned if all caches are loaded and "
                    + "{\"status\":\"INITIALIZING\"} is returned if not. ",
                    notes = "")
    @RequestMapping(value = "/cache_status", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody CacheStatus getCacheStatus() {
        CacheStatus response = new CacheStatus();
        response.setStatus(determineCacheStatus().name());
        return response;
    }

    private CacheStatusName determineCacheStatus() {
        CacheManager manager = CacheManager.getInstance();
        boolean anyPending = false;
        List<String> cacheNames = CacheInitializor.getPreInitializedCaches();
        for (int i = 0; i < cacheNames.size(); i++) {
            Cache currCache = manager.getCache(cacheNames.get(i));
            if (currCache == null || currCache.getKeysNoDuplicateCheck().size() == 0) {
                if (currCache != null) {
                    LOGGER.warn("Cache " + currCache.getName() + " is not yet initialized.");
                } else {
                    LOGGER.warn("Cache " + cacheNames.get(i) + " is null and not initialized.");
                }
                anyPending = true;
            }
        }

        return anyPending ? CacheStatusName.INITIALIZING : CacheStatusName.OK;
    }
}
