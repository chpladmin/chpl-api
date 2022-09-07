package gov.healthit.chpl.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.status.CacheStatusName;
import gov.healthit.chpl.domain.status.ServerStatusName;
import gov.healthit.chpl.domain.status.SystemStatus;
import gov.healthit.chpl.service.DirectReviewSearchService;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "status", description = "Gives insight into system status.")
@RestController
@Log4j2
public class StatusController {

    private DirectReviewSearchService drService;

    @Autowired
    public StatusController(DirectReviewSearchService drService) {
        this.drService = drService;
    }

    @Operation(summary = "Check that the rest services are up and running and indicate whether "
            + "the pre-loaded caches are initializing or have completed."
            + "{\"running\":\"OK\", \"cache\":\"OK\"} is returned if all is well."
            + "If the cache is still initializing, the returned value will be "
            + "{\"running\":\"OK\", \"cache\":\"INITIALIZING\"}.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/system-status", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody SystemStatus getCombinedStatus() {
        SystemStatus response = new SystemStatus();
        // if this code is running then the server is up.
        response.setRunning(ServerStatusName.OK.name());
        // calculate the cache status
        response.setCache(determineCacheStatus().name());
        return response;
    }

    private CacheStatusName determineCacheStatus() {
        return drService.areDirectReviewsLoading() ? CacheStatusName.INITIALIZING : CacheStatusName.OK;
    }
}
