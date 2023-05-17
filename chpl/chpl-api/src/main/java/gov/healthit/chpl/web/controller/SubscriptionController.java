package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.subscription.SubscriptionManager;
import gov.healthit.chpl.subscription.domain.SubscriptionReason;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "subscriptions", description = "Manage public user subscriptions.")
@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {
    private SubscriptionManager subscriptionManager;
    private FF4j ff4j;

    @Autowired
    public SubscriptionController(SubscriptionManager subscriptionManager,
            FF4j ff4j) {
        this.subscriptionManager = subscriptionManager;
        this.ff4j = ff4j;
    }

    @Operation(summary = "Get available subscription reasons.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/reasons", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody List<SubscriptionReason> getSubscriptionReasons() {
        if (!ff4j.check(FeatureList.SUBSCRIPTIONS)) {
            throw new NotImplementedException("The subscriptions feature is not yet implemented.");
        }
        return subscriptionManager.getAllReasons();
    }
}
