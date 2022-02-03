package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.Address;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.DeveloperStatusEvent;
import gov.healthit.chpl.domain.MergeDevelopersRequest;
import gov.healthit.chpl.domain.PermissionDeletedResponse;
import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.domain.SplitDeveloperRequest;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UsersResponse;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.contact.PointOfContact;
import gov.healthit.chpl.domain.developer.hierarchy.DeveloperTree;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.ContactDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
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
import gov.healthit.chpl.service.DirectReviewCachingService;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedResponseFields;
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
    private DirectReviewCachingService directReviewService;
    private FF4j ff4j;

    @Autowired
    public DeveloperController(DeveloperManager developerManager,
            CertifiedProductManager cpManager,
            UserPermissionsManager userPermissionsManager,
            ErrorMessageUtil msgUtil,
            DirectReviewCachingService directReviewService,
            FF4j ff4j) {
        this.developerManager = developerManager;
        this.userPermissionsManager = userPermissionsManager;
        this.msgUtil = msgUtil;
        this.directReviewService = directReviewService;
        this.ff4j = ff4j;
    }

    @Operation(summary = "List all developers in the system.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, and ROLE_ACB can see deleted "
                    + "developers.  Everyone else can only see active developers.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody DeveloperResults getDevelopers(
            @RequestParam(value = "showDeleted", required = false, defaultValue = "false") boolean showDeleted) {
        List<DeveloperDTO> developerList = null;
        if (showDeleted) {
            developerList = developerManager.getAllIncludingDeleted();
        } else {
            developerList = developerManager.getAll();
        }

        List<Developer> developers = new ArrayList<Developer>();
        if (developerList != null && developerList.size() > 0) {
            for (DeveloperDTO dto : developerList) {
                Developer result = new Developer(dto);
                developers.add(result);
            }
        }

        DeveloperResults results = new DeveloperResults();
        results.setDevelopers(developers);
        return results;
    }

    @Operation(summary = "Get information about a specific developer.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{developerId}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @DeprecatedResponseFields(responseClass = Developer.class)
    public @ResponseBody Developer getDeveloperById(@PathVariable("developerId") Long developerId)
            throws EntityRetrievalException {
        DeveloperDTO developer = developerManager.getById(developerId);

        Developer result = null;
        if (developer != null) {
            result = new Developer(developer);
        }
        return result;
    }

    @Operation(summary = "Get all hierarchical information about a specific developer. "
            + "Includes associated products, versions, and basic listing data.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/{developerId}/hierarchy", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    @DeprecatedResponseFields(responseClass = DeveloperTree.class)
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
                directReviewService.getDirectReviews(developerId), HttpStatus.OK);
    }

    @Operation(summary = "Update a developer.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{developerId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    @DeprecatedResponseFields(responseClass = Developer.class)
    public ResponseEntity<Developer> update(@PathVariable("developerId") Long developerId,
            @RequestBody(required = true) Developer developerToUpdate)
            throws InvalidArgumentsException, EntityCreationException, EntityRetrievalException,
            JsonProcessingException, ValidationException, MissingReasonException {
        DeveloperDTO toUpdate = toDto(developerToUpdate);
        toUpdate.setId(developerId);

        DeveloperDTO result = developerManager.update(toUpdate, true);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        if (result == null) {
            throw new EntityCreationException("There was an error inserting or updating the developer information.");
        }
        Developer restResult = new Developer(result);
        return new ResponseEntity<Developer>(restResult, responseHeaders, HttpStatus.OK);
    }

    @Operation(summary = "Merge developers.",
            description = "If multiple developer IDs are passed in, the service performs a merge "
                    + "meaning that a new developer is created with all of the information provided (name, address, "
                    + "etc.) and all of the products previously assigned to the specified developerId's are "
                    + "reassigned to the newly created developer. The old developers are then deleted.\n"
                    + "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB if all developers involved are active.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @DeprecatedResponseFields(responseClass = ChplOneTimeTrigger.class)
    @RequestMapping(value = "/merge", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ChplOneTimeTrigger merge(@RequestBody(required = true) MergeDevelopersRequest mergeRequest)
            throws InvalidArgumentsException, EntityCreationException, EntityRetrievalException, JsonProcessingException,
            ValidationException, SchedulerException {
        if (mergeRequest.getDeveloperIds().size() <= 1) {
            throw new InvalidArgumentsException(
                    "More than 1 developer ID must be present in the request body to perform a merge.");
        }
        DeveloperDTO toCreate = toDto(mergeRequest.getDeveloper());
        return developerManager.merge(mergeRequest.getDeveloperIds(), toCreate);
    }

    @Operation(
            summary = "Split a developer - some products stay with the existing developer and some products are moved "
                    + "to a new developer.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @DeprecatedResponseFields(responseClass = ChplOneTimeTrigger.class)
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
        if (splitRequest.getOldDeveloper().getDeveloperId() != null
                && developerId.longValue() != splitRequest.getOldDeveloper().getDeveloperId().longValue()) {
            throw new InvalidArgumentsException(msgUtil.getMessage("developer.split.requestMismatch"));
        }

        DeveloperDTO oldDeveloper = developerManager.getById(splitRequest.getOldDeveloper().getDeveloperId());
        DeveloperDTO newDeveloper = toDto(splitRequest.getNewDeveloper());
        List<Long> newDeveloperProductIds = new ArrayList<Long>(splitRequest.getNewProducts().size());
        for (Product newDeveloperProduct : splitRequest.getNewProducts()) {
            newDeveloperProductIds.add(newDeveloperProduct.getProductId());
        }

        ChplOneTimeTrigger splitTrigger = developerManager.split(oldDeveloper, newDeveloper, newDeveloperProductIds);
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
        if (!ff4j.check(FeatureList.ROLE_DEVELOPER)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }

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
        if (!ff4j.check(FeatureList.ROLE_DEVELOPER)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }
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

    private DeveloperDTO toDto(Developer developer) {
        DeveloperDTO dto = new DeveloperDTO();
        dto.setDeveloperCode(developer.getDeveloperCode());
        dto.setName(developer.getName());
        dto.setWebsite(developer.getWebsite());
        dto.setSelfDeveloper(developer.getSelfDeveloper());

        if (developer.getStatusEvents() != null && developer.getStatusEvents().size() > 0) {
            for (DeveloperStatusEvent newDeveloperStatusEvent : developer.getStatusEvents()) {
                DeveloperStatusEventDTO statusEvent = new DeveloperStatusEventDTO();
                DeveloperStatusDTO statusDto = new DeveloperStatusDTO();
                statusDto.setId(newDeveloperStatusEvent.getStatus().getId());
                statusDto.setStatusName(newDeveloperStatusEvent.getStatus().getStatus());
                statusEvent.setStatus(statusDto);
                statusEvent.setId(newDeveloperStatusEvent.getId());
                statusEvent.setDeveloperId(newDeveloperStatusEvent.getDeveloperId());
                statusEvent.setReason(newDeveloperStatusEvent.getReason());
                statusEvent.setStatusDate(newDeveloperStatusEvent.getStatusDate());
                dto.getStatusEvents().add(statusEvent);
            }
        }
        Address developerAddress = developer.getAddress();
        if (developerAddress != null) {
            AddressDTO toCreateAddress = new AddressDTO();
            toCreateAddress.setStreetLineOne(developerAddress.getLine1());
            toCreateAddress.setStreetLineTwo(developerAddress.getLine2());
            toCreateAddress.setCity(developerAddress.getCity());
            toCreateAddress.setState(developerAddress.getState());
            toCreateAddress.setZipcode(developerAddress.getZipcode());
            toCreateAddress.setCountry(developerAddress.getCountry());
            dto.setAddress(toCreateAddress);
        }
        PointOfContact developerContact = developer.getContact();
        if (developerContact != null) {
            ContactDTO toCreateContact = new ContactDTO();
            toCreateContact.setEmail(developerContact.getEmail());
            toCreateContact.setFullName(developerContact.getFullName());
            toCreateContact.setPhoneNumber(developerContact.getPhoneNumber());
            toCreateContact.setTitle(developerContact.getTitle());
            dto.setContact(toCreateContact);
        }
        return dto;
    }
}
