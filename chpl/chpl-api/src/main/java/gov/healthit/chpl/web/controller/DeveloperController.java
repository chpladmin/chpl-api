package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.attestation.domain.AttestationPeriodDeveloperException;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.compliance.directreview.DirectReviewCachingService;
import gov.healthit.chpl.developer.join.JoinDevelopersRequest;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.MergeDevelopersRequest;
import gov.healthit.chpl.domain.PermissionDeletedResponse;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.SplitDeveloperRequest;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UsersResponse;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.developer.hierarchy.DeveloperTree;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.JiraRequestFailedException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertifiedProductManager;
import gov.healthit.chpl.manager.DeveloperManager;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApi;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
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
    private UserPermissionsManager userPermissionsManager;
    private AttestationManager attestationManager;
    private DirectReviewCachingService directReviewService;

    @Autowired
    public DeveloperController(DeveloperManager developerManager,
            CertifiedProductManager cpManager,
            UserPermissionsManager userPermissionsManager,
            AttestationManager attestationManager,
            ErrorMessageUtil msgUtil,
            DirectReviewCachingService directReviewService) {
        this.developerManager = developerManager;
        this.userPermissionsManager = userPermissionsManager;
        this.attestationManager = attestationManager;
        this.msgUtil = msgUtil;
        this.directReviewService = directReviewService;
    }

    @Operation(summary = "List all developers in the system.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, and ROLE_ACB can see deleted "
                    + "developers.  Everyone else can only see active developers.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @DeprecatedApiResponseFields(responseClass = DeveloperResults.class, friendlyUrl = "/developers")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody DeveloperResults getDevelopers(
            @RequestParam(value = "showDeleted", required = false, defaultValue = "false") boolean showDeleted) {
        List<Developer> developerList = null;
        if (showDeleted) {
            developerList = developerManager.getAllIncludingDeleted();
        } else {
            developerList = developerManager.getAll();
        }

        DeveloperResults results = new DeveloperResults();
        results.setDevelopers(developerList);
        return results;
    }

    @Operation(summary = "Get information about a specific developer.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @DeprecatedApiResponseFields(responseClass = Developer.class, friendlyUrl = "/developers/{developerId}")
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
    @DeprecatedApiResponseFields(responseClass = DeveloperTree.class, friendlyUrl = "/developers/{developerId}/hierarchy")
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

    @Operation(summary = "List all Real World Testing Plan URLs from active certificates for a developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{developerId}/rwt-plan-urls", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody DeveloperAttestationSubmissionResults getRwtPlanUrls(@PathVariable("developerId") Long developerId) throws InvalidArgumentsException, EntityRetrievalException {
        return null;
    }

    @Operation(summary = "List all Real World Testing Result URLs from active certificates for a developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{developerId}/rwt-result-urls", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody DeveloperAttestationSubmissionResults getRwtResultsUrls(@PathVariable("developerId") Long developerId) throws InvalidArgumentsException, EntityRetrievalException {
        return null;
    }

    @Operation(summary = "Update a developer.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @DeprecatedApiResponseFields(responseClass = Developer.class, httpMethod = "PUT", friendlyUrl = "/developers/{developerId}")
    @RequestMapping(value = "/{developerId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ResponseEntity<Developer> update(@PathVariable("developerId") Long developerId,
            @RequestBody(required = true) Developer developerToUpdate)
            throws InvalidArgumentsException, EntityCreationException, EntityRetrievalException,
            JsonProcessingException, ValidationException, MissingReasonException {
        developerToUpdate.setId(developerId);

        Developer result = developerManager.update(developerToUpdate, true);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
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
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}/join", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger join(@PathVariable("developerId") Long developerId,
            @RequestBody(required = true) JoinDevelopersRequest joinRequest)
            throws InvalidArgumentsException, EntityCreationException, EntityRetrievalException, JsonProcessingException,
            ValidationException, SchedulerException {
        if (CollectionUtils.isEmpty(joinRequest.getDeveloperIds())) {
            throw new InvalidArgumentsException(msgUtil.getMessage("developer.join.missingDeveloperIds"));
        }
        return developerManager.join(developerId, joinRequest.getDeveloperIds());
    }

    @Deprecated
    @DeprecatedApi(friendlyUrl = "/developers/merge", httpMethod = "POST", removalDate = "2023-10-01",
            message = "Developer merge is deprecated and will be removed. "
                    + "Going forward, PUT to /developers/{developerId}/join .")
    @Operation(summary = "Merge developers.",
            description = "Please use /developers/{developerId}/join .",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/merge", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ResponseEntity<Void> merge(@RequestBody(required = true) MergeDevelopersRequest mergeRequest) {
        return new ResponseEntity<>(HttpStatus.MOVED_PERMANENTLY);
    }

    @Operation(
            summary = "Split a developer - some products stay with the existing developer and some products are moved "
                    + "to a new developer.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB",
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

    @Operation(summary = "Remove user permissions from a developer.",
            description = "The logged in user must have ROLE_ADMIN, ROLE_ONC, ROLE_ACB, or ROLE_DEVELOPER "
                    + "and have administrative authority on the "
                    + " specified developer. The user specified in the request will have all authorities "
                    + " removed that are associated with the specified developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "{developerId}/users/{userId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public PermissionDeletedResponse deleteUserFromDeveloper(
            @PathVariable Long developerId, @PathVariable Long userId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        // delete all permissions on that developer
        userPermissionsManager.deleteDeveloperPermission(developerId, userId);
        PermissionDeletedResponse response = new PermissionDeletedResponse();
        response.setPermissionDeleted(true);
        return response;
    }

    @Operation(summary = "List users with permissions on a specified developer.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ACB, or have administrative "
                    + "authority on the specified developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}/users", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody UsersResponse getUsers(@PathVariable("developerId") Long developerId)
            throws InvalidArgumentsException, EntityRetrievalException {
        List<UserDTO> users = developerManager.getAllUsersOnDeveloper(developerId);
        List<User> domainUsers = new ArrayList<User>(users.size());
        for (UserDTO userDto : users) {
            User domainUser = new User(userDto);
            domainUsers.add(domainUser);
        }

        UsersResponse results = new UsersResponse();
        results.setUsers(domainUsers);
        return results;
    }

    @Operation(summary = "List attestations for a developer.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ACB, or have administrative "
                    + "authority on the specified developer.",
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
            description = "Security Restrictions: ROLE_ADMIN, ROLE+_ONC, or ROLE_ONC_ACB",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}/attestations/{attestationPeriodId}/exception", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public AttestationPeriodDeveloperException createAttestationPeriodDeveloperException(@PathVariable("developerId") Long developerId, @PathVariable("attestationPeriodId") Long attestationPeriodId)
            throws EntityRetrievalException, ValidationException {
        return attestationManager.createAttestationPeriodDeveloperException(developerId, attestationPeriodId);
    }
}
