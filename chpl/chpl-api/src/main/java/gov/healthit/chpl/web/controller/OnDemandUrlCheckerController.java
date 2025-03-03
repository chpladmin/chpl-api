package gov.healthit.chpl.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.datadog.api.client.ApiException;

import gov.healthit.chpl.datadog.OnDemandUrlCheckerManager;
import gov.healthit.chpl.datadog.OnDemandUrlCheckerResponse;
import gov.healthit.chpl.datadog.OnDemandUrlRequest;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "urls", description = "")
@RestController
@RequestMapping("/urls")
public class OnDemandUrlCheckerController {

    private OnDemandUrlCheckerManager onDemandUrlCheckerManager;

    @Autowired
    public OnDemandUrlCheckerController(OnDemandUrlCheckerManager onDemandUrlCheckerManager) {
        this.onDemandUrlCheckerManager = onDemandUrlCheckerManager;
    }

    @Operation(summary = "Validates a URL.  Three checks are performed: 1) HTTP Status code is 200, 2) Response time is less than 30 seconds, and 3) Response body is not empty.",
            description = "Security Restrictions: chpl-admin, chpl-onc, or chpl-onc-acb",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
    })
    @RequestMapping(value = "/validate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public OnDemandUrlCheckerResponse checkUrl(@RequestBody OnDemandUrlRequest url) throws InterruptedException, ApiException, ValidationException {
        return onDemandUrlCheckerManager.checkUrl(url.getUrl());
    }

}
