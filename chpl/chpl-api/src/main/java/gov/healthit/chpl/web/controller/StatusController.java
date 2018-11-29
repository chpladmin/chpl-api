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
public class StatusController {
    private static final Logger LOGGER = LogManager.getLogger(StatusController.class);

    /**
     * Get the status, indicating if the server is running at all.
     * @return JSON value that indicates the server is running
     */
    @ApiOperation(
            value = "Check that the rest services are up and running."
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
    @ApiOperation(
            value = "Check the status of every cache. "
                    + "{\"status\":\"OK\"} is returned if all caches are loaded and "
                    + "{\"status\":\"INITIALIZING\"} is returned if not. ",
                    notes = "")
    @RequestMapping(value = "/cache_status", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody CacheStatus getCacheStatus() {
        CacheStatus response = new CacheStatus();
        CacheManager manager = CacheManager.getInstance();
        boolean anyPending = false;
        List<String> cacheNames = CacheInitializor.getPreInitializedCaches();
        for (int i = 0; i < cacheNames.size(); i++) {
            Cache currCache = manager.getCache(cacheNames.get(i));
            if (currCache == null || currCache.getKeysNoDuplicateCheck().size() == 0) {
                anyPending = true;
            }
        }
        if (anyPending) {
            response.setStatus(CacheStatusName.INITIALIZING.name());
        } else {
            response.setStatus(CacheStatusName.OK.name());
        }
        return response;
    }
}
