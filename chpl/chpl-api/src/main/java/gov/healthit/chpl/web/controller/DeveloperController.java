package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.core.JacksonException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.compliance.directreview.DirectReviewCachingService;
import gov.healthit.chpl.developer.join.JoinDevelopersRequest;
import gov.healthit.chpl.developer.messaging.DeveloperMessageRequest;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.SplitDeveloperRequest;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UsersResponse;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.developer.hierarchy.DeveloperTree;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.JiraRequestFailedException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.insight.InsightRequestFailedException;
import gov.healthit.chpl.insight.InsightService;
import gov.healthit.chpl.insight.InsightSubmission;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.realworldtesting.domain.RealWorldTestingUrlByDeveloper;
import gov.healthit.chpl.realworldtesting.manager.RealWorldTestingManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.results.DeveloperAttestationSubmissionResults;
import gov.healthit.chpl.web.controller.results.DeveloperResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "developers", description = "Allows management of developers, their users, and retrieval of their direct reviews.")
@RestController
@RequestMapping("/developers")
@Log4j2
public class DeveloperController {

    private DeveloperManager developerManager;
    private ErrorMessageUtil msgUtil;
    private AttestationManager attestationManager;
    private InsightService insightsService;
    private DirectReviewCachingService directReviewService;
    private RealWorldTestingManager rwtManager;
    private FF4j ff4j;

    @Autowired
    public DeveloperController(DeveloperManager developerManager,
            CertifiedProductManager cpManager,
            AttestationManager attestationManager,
            InsightService insightsService,
            ErrorMessageUtil msgUtil,
            DirectReviewCachingService directReviewService,
            RealWorldTestingManager rwtManager,
            FF4j ff4j) {
        this.developerManager = developerManager;
        this.attestationManager = attestationManager;
        this.insightsService = insightsService;
        this.msgUtil = msgUtil;
        this.directReviewService = directReviewService;
        this.rwtManager = rwtManager;
        this.ff4j = ff4j;
    }

    @Operation(summary = "List all developers in the system.",
            description = "List all developers in the system.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody DeveloperResults getDevelopers() {
        List<Developer> developerList = developerManager.getAll();
        DeveloperResults results = new DeveloperResults();
        results.setDevelopers(developerList);
        return results;
    }

    @Operation(summary = "Get information about a specific developer.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{developerId:^-?\\d+$}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Developer getDeveloperById(@PathVariable("developerId") Long developerId)
            throws EntityRetrievalException {
        return developerManager.getById(developerId);
    }

    @Operation(summary = "Get all hierarchical information about a specific developer. "
            + "Includes associated products, versions, and basic listing data.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{developerId}/hierarchy", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody DeveloperTree getDeveloperHierarchyById(@PathVariable("developerId") Long developerId)
            throws EntityRetrievalException {
        return developerManager.getHierarchyById(developerId);
    }

    @Operation(summary = "Get all direct reviews for a specified developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{developerId:^-?\\d+$}/direct-reviews",
            method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ResponseEntity<List<DirectReview>> getDirectReviews(
            @PathVariable("developerId") Long developerId) throws JiraRequestFailedException {
        return new ResponseEntity<List<DirectReview>>(
                directReviewService.getDirectReviews(developerId).getDirectReviews(), HttpStatus.OK);
    }

    @Operation(summary = "List Insight sumbissions for a developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}/insights", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ResponseEntity<List<InsightSubmission>> getInsights(@PathVariable("developerId") Long developerId)
            throws InsightRequestFailedException, EntityRetrievalException {
        if (!ff4j.check(FeatureList.INSIGHTS_DISPLAY)) {
            throw new NotImplementedException("This method has not been implemented");
        }
        return new ResponseEntity<List<InsightSubmission>>(insightsService.getInsightSubmissions(developerId), HttpStatus.OK);
    }

    @Operation(summary = "List all Real World Testing Plans URLs from active certificates for a developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{developerId}/rwt-plans-urls", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RealWorldTestingUrlByDeveloper> getRwtPlansUrls(@PathVariable("developerId") Long developerId) throws InvalidArgumentsException, EntityRetrievalException {
        return rwtManager.getPlansUrls(developerId);
    }

    @Operation(summary = "List all Real World Testing Results URLs from active certificates for a developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{developerId}/rwt-results-urls", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<RealWorldTestingUrlByDeveloper> getRwtResultsUrls(@PathVariable("developerId") Long developerId) throws InvalidArgumentsException, EntityRetrievalException {
        return rwtManager.getResultsUrls(developerId);
    }

    @Operation(summary = "Update a developer.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, or chpl-onc-acb",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ResponseEntity<Developer> update(@PathVariable("developerId") Long developerId, @RequestBody(required = true) Developer developerToUpdate)
            throws EntityRetrievalException, ValidationException, EntityCreationException, ActivityException {

        developerToUpdate.setId(developerId);

        Developer result = developerManager.update(developerToUpdate, true);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_SEARCH);
        if (result == null) {
            throw new EntityCreationException("There was an error inserting or updating the developer information.");
        }
        return new ResponseEntity<Developer>(result, responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "Updates one or more developers to join another developer organization.",
            description = "Updates all products owned by the developers in the request body to become "
                    + "owned by the developer on the request URL. The product ownership history "
                    + "for all relevant products is updated and any listings under those products "
                    + "with newer-style CHPL Product Numbers will have their CHPL Product Numbers "
                    + "updated using the Developer Code of the developer being joined. This endpoint will "
                    + "cause an email to be sent to the user making the request when the work is complete."
                    + "Security Restrictions: Users with either chpl-admin or chpl-onc",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}/join", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger join(@PathVariable("developerId") Long developerId,
            @RequestBody(required = true) JoinDevelopersRequest joinRequest)
            throws InvalidArgumentsException, EntityCreationException, EntityRetrievalException, JacksonException,
            ValidationException, SchedulerException {
        if (CollectionUtils.isEmpty(joinRequest.getDeveloperIds())) {
            throw new InvalidArgumentsException(msgUtil.getMessage("developer.join.missingDeveloperIds"));
        }
        return developerManager.join(developerId, joinRequest.getDeveloperIds());
    }

    @Operation(
            summary = "Split a developer - some products stay with the existing developer and some products are moved "
                    + "to a new developer.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, or chpl-onc-acb",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}/split", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger splitDeveloper(@PathVariable("developerId") Long developerId,
            @RequestBody(required = true) SplitDeveloperRequest splitRequest)
            throws EntityRetrievalException, InvalidArgumentsException, ValidationException, SchedulerException {

        // validate required fields are present in the split request
        // new developer product ids cannot be empty
        if (splitRequest.getNewProducts() == null || splitRequest.getNewProducts().size() == 0) {
            String error = msgUtil.getMessage("developer.split.missingNewDeveloperProducts");
            throw new InvalidArgumentsException(error);
        }
        // old developer product ids cannot be empty
        if (splitRequest.getOldProducts() == null || splitRequest.getOldProducts().size() == 0) {
            String error = msgUtil.getMessage("developer.split.missingOldDeveloperProducts");
            throw new InvalidArgumentsException(error);
        }
        // new and old developers cannot be empty
        if (splitRequest.getNewDeveloper() == null || splitRequest.getOldDeveloper() == null) {
            String error = msgUtil.getMessage("developer.split.newAndOldDeveloperRequired");
            throw new InvalidArgumentsException(error);
        }
        // make sure the developer id in the split request matches the developer
        // id on the url path
        if (splitRequest.getOldDeveloper().getId() != null
                && developerId.longValue() != splitRequest.getOldDeveloper().getId().longValue()) {
            throw new InvalidArgumentsException(msgUtil.getMessage("developer.split.requestMismatch"));
        }

        Developer oldDeveloper = developerManager.getById(splitRequest.getOldDeveloper().getId());
        List<Long> newDeveloperProductIds = new ArrayList<Long>(splitRequest.getNewProducts().size());
        for (Product newDeveloperProduct : splitRequest.getNewProducts()) {
            newDeveloperProductIds.add(newDeveloperProduct.getId());
        }

        ChplOneTimeTrigger splitTrigger = developerManager.split(oldDeveloper, splitRequest.getNewDeveloper(), newDeveloperProductIds);
        return splitTrigger;
    }

    @Operation(summary = "List users with permissions on a specified developer.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, "
                    + ", chpl-onc-acb or chpl-developer with administrative authority on the specified developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}/users", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody UsersResponse getUsers(@PathVariable("developerId") Long developerId)
            throws InvalidArgumentsException, EntityRetrievalException {
        List<User> domainUsers = developerManager.getAllUsersOnDeveloper(developerId);
        UsersResponse results = new UsersResponse();
        results.setUsers(domainUsers);
        return results;
    }

    @Operation(summary = "List attestations for a developer.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, "
                    + ", chpl-onc-acb or chpl-developer with administrative authority on the specified developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}/attestations", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody DeveloperAttestationSubmissionResults getAttestations(@PathVariable("developerId") Long developerId) throws InvalidArgumentsException, EntityRetrievalException {
        return DeveloperAttestationSubmissionResults.builder()
                .attestations(attestationManager.getDeveloperAttestations(developerId))
                .submittablePeriod(attestationManager.getSubmittablePeriod(developerId))
                .canCreateException(attestationManager.canCreateException(developerId))
                .build();
    }

    @Operation(summary = "Create a new attestation submission end date exception for a developer.",
            description = "Security Restrictions: Users with either role chpl-admin, chpl-onc, or chpl-onc-acb",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}/attestations/{attestationPeriodId}/exception", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public AttestationPeriodDeveloperException createAttestationPeriodDeveloperException(@PathVariable("developerId") Long developerId, @PathVariable("attestationPeriodId") Long attestationPeriodId)
            throws EntityRetrievalException, ValidationException {
        return attestationManager.createAttestationPeriodDeveloperException(developerId, attestationPeriodId);
    }

    @Operation(summary = "Sends a message to all developers matching the provided search parameters. "
            + "A report of the message sent and recipients will be sent to the requestor after completion.",
            description = "Security Restrictions: Users with either role chpl-admin or chpl-onc",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/messages", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger sendMessage(@RequestBody(required = true) DeveloperMessageRequest developerMessageRequest)
        throws ValidationException, SchedulerException {
        return developerManager.triggerMessageDevelopers(developerMessageRequest);
    }

    @Operation(summary = "Sends a single message to the logged-in user. "
            + "This allows the logged-in user to preview how an email will appear to developers",
            description = "Security Restrictions: Users with either role chpl-admin or chpl-onc",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/message-preview", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger sendMessagePreview(@RequestBody(required = true) DeveloperMessageRequest developerMessageRequest)
        throws ValidationException, SchedulerException {
        return developerManager.triggerMessageDevelopersPreview(developerMessageRequest);
    }
}
