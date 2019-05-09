package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.PermittedUser;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import gov.healthit.chpl.web.controller.results.CertificationBodyResults;
import gov.healthit.chpl.web.controller.results.PermittedUserResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "acbs")
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

    @ApiOperation(value = "List all certification bodies (ACBs).",
            notes = "Setting the 'editable' parameter to true will return all ACBs that the logged in user has "
                    + "edit permissions on. Security Restrictions:  All users can see all active ACBs.")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody CertificationBodyResults getAcbs(
            @RequestParam(required = false, defaultValue = "false") final boolean editable) {
        //TODO confirm a user is logged in here
        CertificationBodyResults results = new CertificationBodyResults();
        List<CertificationBodyDTO> acbs = null;
        if (editable) {
            acbs = resourcePermissions.getAllAcbsForCurrentUser();
        } else {
            acbs = acbManager.getAll();
        }

        if (acbs != null) {
            for (CertificationBodyDTO acb : acbs) {
                results.getAcbs().add(new CertificationBody(acb));
            }
        }
        return results;
    }

    @ApiOperation(value = "Get details about a specific certification body (ACB).",
            notes = "")
    @RequestMapping(value = "/{acbId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody CertificationBody getAcbById(@PathVariable("acbId") final Long acbId)
            throws EntityRetrievalException {
        CertificationBodyDTO acb = acbManager.getById(acbId);

        return new CertificationBody(acb);
    }

    @ApiOperation(value = "Create a new ACB.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ONC")
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public CertificationBody createAcb(@RequestBody final CertificationBody acbInfo)
            throws InvalidArgumentsException, UserRetrievalException, EntityRetrievalException, EntityCreationException,
            JsonProcessingException {

        return create(acbInfo);
    }

    private CertificationBody create(final CertificationBody acbInfo)
            throws InvalidArgumentsException, UserRetrievalException, EntityRetrievalException,
            EntityCreationException, JsonProcessingException {

        CertificationBodyDTO toCreate = new CertificationBodyDTO();
        toCreate.setAcbCode(acbInfo.getAcbCode());
        toCreate.setName(acbInfo.getName());
        if (StringUtils.isEmpty(acbInfo.getWebsite())) {
            throw new InvalidArgumentsException("A website is required for a new certification body");
        }
        toCreate.setWebsite(acbInfo.getWebsite());

        if (acbInfo.getAddress() == null) {
            throw new InvalidArgumentsException("An address is required for a new certification body");
        }
        AddressDTO address = new AddressDTO();
        address.setId(acbInfo.getAddress().getAddressId());
        address.setStreetLineOne(acbInfo.getAddress().getLine1());
        address.setStreetLineTwo(acbInfo.getAddress().getLine2());
        address.setCity(acbInfo.getAddress().getCity());
        address.setState(acbInfo.getAddress().getState());
        address.setZipcode(acbInfo.getAddress().getZipcode());
        address.setCountry(acbInfo.getAddress().getCountry());
        toCreate.setAddress(address);
        toCreate = acbManager.create(toCreate);
        return new CertificationBody(toCreate);
    }


    @ApiOperation(value = "Update an existing ACB.",
            notes = "Security Restriction:  ROLE_ADMIN, ROLE_ONC, or ROLE_ACB with administrative authority")
    @RequestMapping(value = "/{acbId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public CertificationBody updateAcb(@RequestBody final CertificationBody acbInfo) throws InvalidArgumentsException,
            EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException,
            SchedulerException, ValidationException {

        return update(acbInfo);
    }

    private CertificationBody update(final CertificationBody updatedAcb) throws InvalidArgumentsException,
            EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException,
            SchedulerException, ValidationException {
        //Get the ACB as it is currently in the database to find out if
        //the retired flag was changed.
        //Retirement and un-retirement is done as a separate manager action because
        //security is different from normal ACB updates - only admins are allowed
        //whereas an ACB admin can update other info
        CertificationBodyDTO existingAcb = resourcePermissions.getAcbIfPermissionById(updatedAcb.getId());
        if (updatedAcb.isRetired()) {
            //we are retiring this ACB - no other updates can happen
            existingAcb.setRetirementDate(updatedAcb.getRetirementDate());
            existingAcb.setRetired(true);
            acbManager.retire(existingAcb);
        } else {
            if (existingAcb.isRetired()) {
                //unretire the ACB
                acbManager.unretire(updatedAcb.getId());
            }
            CertificationBodyDTO toUpdate = new CertificationBodyDTO();
            toUpdate.setId(updatedAcb.getId());
            toUpdate.setAcbCode(updatedAcb.getAcbCode());
            toUpdate.setName(updatedAcb.getName());
            toUpdate.setRetired(false);
            toUpdate.setRetirementDate(null);
            if (StringUtils.isEmpty(updatedAcb.getWebsite())) {
                throw new InvalidArgumentsException("A website is required to update the certification body");
            }
            toUpdate.setWebsite(updatedAcb.getWebsite());

            if (updatedAcb.getAddress() == null) {
                throw new InvalidArgumentsException("An address is required to update the certification body");
            }
            AddressDTO address = new AddressDTO();
            address.setId(updatedAcb.getAddress().getAddressId());
            address.setStreetLineOne(updatedAcb.getAddress().getLine1());
            address.setStreetLineTwo(updatedAcb.getAddress().getLine2());
            address.setCity(updatedAcb.getAddress().getCity());
            address.setState(updatedAcb.getAddress().getState());
            address.setZipcode(updatedAcb.getAddress().getZipcode());
            address.setCountry(updatedAcb.getAddress().getCountry());
            toUpdate.setAddress(address);
            acbManager.update(toUpdate);
        }
        CertificationBodyDTO result = acbManager.getById(updatedAcb.getId());
        return new CertificationBody(result);
    }

    @ApiOperation(value = "Remove user permissions from an ACB.",
            notes = "The logged in user must have ROLE_ADMIN or ROLE_ACB and have administrative authority on the "
                    + " specified ACB. The user specified in the request will have all authorities "
                    + " removed that are associated with the specified ACB.")
    @RequestMapping(value = "{acbId}/users/{userId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public String deleteUserFromAcb(@PathVariable final Long acbId, @PathVariable final Long userId)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        return deleteUser(acbId, userId);
    }

    private String deleteUser(final Long acbId, final Long userId)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        UserDTO user = userManager.getById(userId);
        CertificationBodyDTO acb = resourcePermissions.getAcbIfPermissionById(acbId);

        if (user == null || acb == null) {
            throw new InvalidArgumentsException("Could not find either ACB or User specified");
        }

        // delete all permissions on that acb
        userPermissionsManager.deleteAcbPermission(acb, userId);

        return "{\"userDeleted\" : true}";
    }

    @ApiOperation(value = "List users with permissions on a specified ACB.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or have administrative "
                    + "or read authority on the specified ACB")
    @RequestMapping(value = "/{acbId}/users", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody PermittedUserResults getUsers(@PathVariable("acbId") final Long acbId)
            throws InvalidArgumentsException, EntityRetrievalException {
        CertificationBodyDTO acb = resourcePermissions.getAcbIfPermissionById(acbId);
        if (acb == null) {
            throw new InvalidArgumentsException("Could not find the ACB specified.");
        }

        List<PermittedUser> acbUsers = new ArrayList<PermittedUser>();
        List<UserDTO> users = resourcePermissions.getAllUsersOnAcb(acb);
        for (UserDTO user : users) {

            // only show users that have ROLE_ACB
            Set<UserPermissionDTO> systemPermissions = userManager.getGrantedPermissionsForUser(user);
            boolean hasAcbPermission = false;
            for (UserPermissionDTO systemPermission : systemPermissions) {
                if (systemPermission.getAuthority().equals("ROLE_ACB")) {
                    hasAcbPermission = true;
                }
            }

            if (hasAcbPermission) {
                List<String> roleNames = new ArrayList<String>();
                for (UserPermissionDTO role : systemPermissions) {
                    roleNames.add(role.getAuthority());
                }

                PermittedUser userInfo = new PermittedUser();
                userInfo.setUser(new User(user));
                userInfo.setRoles(roleNames);
                acbUsers.add(userInfo);
            }
        }

        PermittedUserResults results = new PermittedUserResults();
        results.setUsers(acbUsers);
        return results;
    }
}
