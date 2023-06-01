package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UsersResponse;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import gov.healthit.chpl.web.controller.results.CertificationBodyResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "acbs", description = "Allows CRUD operations on certification bodies (ONC-ACBs).")
@RestController
@RequestMapping("/acbs")
public class CertificationBodyController {

    @Autowired
    private CertificationBodyManager acbManager;

    @Autowired
    private ResourcePermissions resourcePermissions;

    @Autowired
    private UserPermissionsManager userPermissionsManager;

    @Autowired
    private UserManager userManager;

    @Operation(summary = "List all certification bodies (ONC-ACBs).",
            description = "Setting the 'editable' parameter to true will return all ONC-ACBs that the logged in user has "
                    + "edit permissions on. Security Restrictions:  All users can see all active ONC-ACBs.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The ONC-ACBs request was successful.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CertificationBodyResults.class))
                    }),
            @ApiResponse(responseCode = "500", description = "There was an unexpected error getting all ONC-ACBs.",
                    content = @Content)
    })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertificationBodyResults getAcbs(
            @RequestParam(required = false, defaultValue = "false") final boolean editable) {
        // TODO confirm a user is logged in here
        CertificationBodyResults results = new CertificationBodyResults();
        List<CertificationBody> acbs = null;
        if (editable) {
            acbs = resourcePermissions.getAllAcbsForCurrentUser();
        } else {
            acbs = acbManager.getAll();
        }

        if (acbs != null) {
            results.getAcbs().addAll(acbs);
        }
        return results;
    }

    @Operation(summary = "Get details about a specific ONC-ACB.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The ONC-ACB ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CertificationBody.class))
                    }),
            @ApiResponse(responseCode = "404", description = "The ONC-ACB ID given on the URL is invalid.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "There was an unexpected error getting the ONC-ACB..",
                    content = @Content)
    })
    @RequestMapping(value = "/{acbId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody CertificationBody getAcbById(@PathVariable("acbId") final Long acbId)
            throws EntityRetrievalException {
        return acbManager.getById(acbId);
    }

    @Operation(summary = "Create a new ONC-ACB.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ONC",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The ONC-ACB ID was valid.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CertificationBody.class))
                    }),
            @ApiResponse(responseCode = "401", description = "The authenticated user does not have permissions to create an ONC-ACB.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "There was an unexpected error creating the ONC-ACB.",
                    content = @Content)
    })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public CertificationBody createAcb(@RequestBody final CertificationBody acbInfo)
            throws InvalidArgumentsException, UserRetrievalException, EntityRetrievalException, EntityCreationException,
            JsonProcessingException {
        CertificationBody toCreate = new CertificationBody();
        toCreate.setAcbCode(acbInfo.getAcbCode());
        toCreate.setName(acbInfo.getName());
        if (ObjectUtils.isEmpty(acbInfo.getWebsite())) {
            throw new InvalidArgumentsException("A website is required for a new certification body");
        }
        toCreate.setWebsite(acbInfo.getWebsite());

        if (acbInfo.getAddress() == null) {
            throw new InvalidArgumentsException("An address is required for a new certification body");
        }
        toCreate.setAddress(acbInfo.getAddress());
        return acbManager.create(toCreate);
    }

    @Operation(summary = "Update an existing ONC-ACB.",
            description = "Security Restriction: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB with administrative authority.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The ONC-ACB data was updated.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = CertificationBody.class))
                    }),
            @ApiResponse(responseCode = "401", description = "The user making the request does not have permission to update the ONC-ACB.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "The ONC-ACB was not found in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "There was an unexpected error when updating the ONC-ACB.",
                    content = @Content)
    })
    @RequestMapping(value = "/{acbId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public CertificationBody updateAcb(@RequestBody final CertificationBody acbToUpdate) throws InvalidArgumentsException,
            EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException,
            SchedulerException, ValidationException {

        // Get the ACB as it is currently in the database to find out if
        // the retired flag was changed.
        // Retirement and un-retirement is done as a separate manager action
        // because security is different from normal ACB updates - only admins are
        // allowed whereas an ACB admin can update other info
        CertificationBody existingAcb = resourcePermissions.getAcbIfPermissionById(acbToUpdate.getId());
        if (acbToUpdate.isRetired()) {
            // we are retiring this ACB - no other updates can happen
            existingAcb.setRetirementDate(acbToUpdate.getRetirementDate());
            existingAcb.setRetired(true);
            acbManager.retire(existingAcb);
        } else {
            if (existingAcb.isRetired()) {
                // unretire the ACB
                acbManager.unretire(acbToUpdate.getId());
            }

            if (StringUtils.isEmpty(acbToUpdate.getWebsite())) {
                throw new InvalidArgumentsException("A website is required to update the certification body");
            }
            if (acbToUpdate.getAddress() == null) {
                throw new InvalidArgumentsException("An address is required to update the certification body");
            }
            acbManager.update(acbToUpdate);
        }
        return acbManager.getById(acbToUpdate.getId());
    }

    @Operation(summary = "Remove user permissions from an ONC-ACB.",
            description = "The logged in user must have ROLE_ADMIN or ROLE_ACB and have administrative authority on the "
                    + " specified ONC-ACB. The user specified in the request will have all authorities "
                    + " removed that are associated with the specified ONC-ACB.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The permissions were successfully removed.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = String.class))
                    }),
            @ApiResponse(responseCode = "401", description = "The authenticated user does not have permissions to complete the action.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "The ONC-ACB ID specified in the URL does not exist in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "There was an unexpected error updating user permissions.",
                    content = @Content)
    })
    @RequestMapping(value = "{acbId}/users/{userId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public String deleteUserFromAcb(@PathVariable final Long acbId, @PathVariable final Long userId)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException, JsonProcessingException, EntityCreationException {
        UserDTO user = userManager.getById(userId);
        CertificationBody acb = resourcePermissions.getAcbIfPermissionById(acbId);

        if (user == null || acb == null) {
            throw new InvalidArgumentsException("Could not find either ACB or User specified");
        }

        // delete all permissions on that acb
        userPermissionsManager.deleteAcbPermission(acb, userId);

        return "{\"userDeleted\" : true}";
    }

    @Operation(summary = "List users with permissions on a specified ONC-ACB.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or have administrative "
                    + "or read authority on the specified ONC-ACB",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The request was successful.",
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = UsersResponse.class))
                    }),
            @ApiResponse(responseCode = "401", description = "The authenticated user does not have permissions to get the user list.",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "The ONC-ACB ID specified in the URL does not exist in the CHPL database.",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "There was an unexpected error getting the user list.",
                    content = @Content)
    })
    @RequestMapping(value = "/{acbId}/users", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody UsersResponse getUsers(@PathVariable("acbId") final Long acbId)
            throws InvalidArgumentsException, EntityRetrievalException {
        CertificationBody acb = resourcePermissions.getAcbIfPermissionById(acbId);
        if (acb == null) {
            throw new InvalidArgumentsException("Could not find the ACB specified.");
        }

        List<UserDTO> users = resourcePermissions.getAllUsersOnAcb(acb);
        List<User> acbUsers = new ArrayList<User>(users.size());
        for (UserDTO userDto : users) {
            User acbUser = new User(userDto);
            acbUsers.add(acbUser);
        }

        UsersResponse results = new UsersResponse();
        results.setUsers(acbUsers);
        return results;
    }
}
