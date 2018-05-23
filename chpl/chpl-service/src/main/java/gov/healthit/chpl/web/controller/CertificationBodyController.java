package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.Permission;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.dto.UserPermissionDTO;
import gov.healthit.chpl.auth.json.User;
import gov.healthit.chpl.auth.manager.UserManager;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.ChplPermission;
import gov.healthit.chpl.domain.PermittedUser;
import gov.healthit.chpl.domain.UpdateUserAndAcbRequest;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.manager.impl.UpdateCertifiedBodyException;
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
    private UserManager userManager;

    private static final Logger LOGGER = LogManager.getLogger(CertificationBodyController.class);

    @ApiOperation(value = "List all certification bodies (ACBs).",
            notes = "Setting the 'editable' parameter to true will return all ACBs that the logged in user has "
                    + "edit permissions on.  Setting 'showDeleted' to true will include even those ACBs that "
                    + "have been deleted. The logged in user must have ROLE_ADMIN to see deleted ACBs. The default "
                    + "behavior of this service is to list all of the ACBs in the system that are not deleted.")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody CertificationBodyResults getAcbs(
            @RequestParam(required = false, defaultValue = "false") final boolean editable,
            @RequestParam(value = "showDeleted", required = false, defaultValue = "false") final boolean showDeleted) {
        CertificationBodyResults results = new CertificationBodyResults();
        if (!Util.isUserRoleAdmin() && showDeleted) {
            throw new AccessDeniedException("Only Admins can see deleted ACB's.");
        } else {
            results = new CertificationBodyResults();
            List<CertificationBodyDTO> acbs = null;
            if (editable) {
                acbs = acbManager.getAllForUser(showDeleted);
            } else {
                acbs = acbManager.getAll(showDeleted);
            }

            if (acbs != null) {
                for (CertificationBodyDTO acb : acbs) {
                    results.getAcbs().add(new CertificationBody(acb));
                }
            }
        }
        return results;
    }

    @ApiOperation(value = "Get details about a specific certification body (ACB).",
            notes = "The logged in user must either have ROLE_ADMIN or have ROLE_ACB "
                    + " for the ACB with the provided ID.")
    @RequestMapping(value = "/{acbId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody CertificationBody getAcbById(@PathVariable("acbId") final Long acbId)
            throws EntityRetrievalException {
        CertificationBodyDTO acb = acbManager.getById(acbId);

        return new CertificationBody(acb);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Create a new ACB.", notes = "The logged in user must have ROLE_ADMIN to "
            + "create a new ACB.")
    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public CertificationBody createAcbDeprecated(@RequestBody final CertificationBody acbInfo)
            throws InvalidArgumentsException, UserRetrievalException, EntityRetrievalException, EntityCreationException,
            JsonProcessingException {

        return create(acbInfo);
    }

    @ApiOperation(value = "Create a new ACB.", notes = "The logged in user must have ROLE_ADMIN to create a new ACB.")
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

    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Update an existing ACB.",
            notes = "The logged in user must either have ROLE_ADMIN or have ROLE_ACB "
                    + " to update an existing ACB.")
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public CertificationBody updateAcbDeprecated(@RequestBody final CertificationBody acbInfo)
            throws InvalidArgumentsException, EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateCertifiedBodyException {

        return update(acbInfo);
    }

    @ApiOperation(value = "Update an existing ACB.",
            notes = "The logged in user must either have ROLE_ADMIN or have ROLE_ACB "
                    + " to update an existing ACB.")
    @RequestMapping(value = "/{acbId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public CertificationBody updateAcb(@RequestBody final CertificationBody acbInfo) throws InvalidArgumentsException,
            EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException {

        return update(acbInfo);
    }

    private CertificationBody update(final CertificationBody acbInfo) throws InvalidArgumentsException,
            EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateCertifiedBodyException {
        CertificationBodyDTO toUpdate = new CertificationBodyDTO();
        toUpdate.setId(acbInfo.getId());
        toUpdate.setAcbCode(acbInfo.getAcbCode());
        toUpdate.setName(acbInfo.getName());
        if (StringUtils.isEmpty(acbInfo.getWebsite())) {
            throw new InvalidArgumentsException("A website is required to update the certification body");
        }
        toUpdate.setWebsite(acbInfo.getWebsite());

        if (acbInfo.getAddress() == null) {
            throw new InvalidArgumentsException("An address is required to update the certification body");
        }
        AddressDTO address = new AddressDTO();
        address.setId(acbInfo.getAddress().getAddressId());
        address.setStreetLineOne(acbInfo.getAddress().getLine1());
        address.setStreetLineTwo(acbInfo.getAddress().getLine2());
        address.setCity(acbInfo.getAddress().getCity());
        address.setState(acbInfo.getAddress().getState());
        address.setZipcode(acbInfo.getAddress().getZipcode());
        address.setCountry(acbInfo.getAddress().getCountry());
        toUpdate.setAddress(address);

        CertificationBodyDTO result = acbManager.update(toUpdate);
        return new CertificationBody(result);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Delete an ACB.", notes = "The logged in user must have ROLE_ADMIN.")
    @RequestMapping(value = "/{acbId}/delete", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public String deleteAcbDeprecated(@PathVariable("acbId") final Long acbId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        return delete(acbId);
    }

    @ApiOperation(value = "Delete an ACB.", notes = "The logged in user must have ROLE_ADMIN.")
    @RequestMapping(value = "/{acbId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public String deleteAcbd(@PathVariable("acbId") final Long acbId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        return delete(acbId);
    }

    private String delete(final Long acbId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        CertificationBodyDTO toDelete = acbManager.getById(acbId);
        acbManager.delete(toDelete);
        return "{\"deletedAcb\" : true}";
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Restore a deleted ACB.",
            notes = "ACBs are unique in the CHPL in that they can be restored after a delete."
                    + " The logged in user must have ROLE_ADMIN.")
    @RequestMapping(value = "/{acbId}/undelete", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public String undeleteAcbDeprecated(@PathVariable("acbId") final Long acbId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        return undelete(acbId);
    }

    @ApiOperation(value = "Restore a deleted ACB.",
            notes = "ACBs are unique in the CHPL in that they can be restored after a delete."
                    + " The logged in user must have ROLE_ADMIN.")
    @RequestMapping(value = "/{acbId}/undelete", method = RequestMethod.PUT,
            produces = "application/json; charset=utf-8")
    public String undeleteAcb(@PathVariable("acbId") final Long acbId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        return undelete(acbId);
    }

    private String undelete(final Long acbId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        CertificationBodyDTO toResurrect = acbManager.getById(acbId, true);
        acbManager.undelete(toResurrect);
        return "{\"resurrectedAcb\" : true}";
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Add a user to an ACB.",
            notes = "The logged in user must have ROLE_ADMIN or ROLE_ACB and have administrative authority on the "
                    + " specified ACB. It is recommended to pass 'ADMIN' in as the 'authority' field"
                    + " to guarantee maximum compatibility although 'READ' and 'DELETE' are also valid choices. "
                    + " Note that this method gives special permission on a specific ACB and is not the "
                    + " equivalent of assigning the ROLE_ACB role. Please view /users/grant_role "
                    + " request for more information on that.")
    @RequestMapping(value = "/add_user", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public String addUserToAcbDeprecated(@RequestBody final UpdateUserAndAcbRequest updateRequest)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        return addUser(updateRequest);
    }

    @ApiOperation(value = "Add a user to an ACB.",
            notes = "The logged in user must have ROLE_ADMIN or ROLE_ACB and have administrative authority on the "
                    + " specified ACB. It is recommended to pass 'ADMIN' in as the 'authority' field"
                    + " to guarantee maximum compatibility although 'READ' and 'DELETE' are also valid choices. "
                    + " Note that this method gives special permission on a specific ACB and is not the "
                    + " equivalent of assigning the ROLE_ACB role. Please view /users/grant_role "
                    + " request for more information on that.")
    @RequestMapping(value = "/{acbId}/users", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public String addUserToAcb(@RequestBody final UpdateUserAndAcbRequest updateRequest)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        return addUser(updateRequest);
    }

    public String addUser(final UpdateUserAndAcbRequest updateRequest)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        if (updateRequest.getAcbId() == null || updateRequest.getUserId() == null || updateRequest.getUserId() <= 0
                || updateRequest.getAuthority() == null) {
            throw new InvalidArgumentsException("ACB ID, User ID (greater than 0), and Authority are required.");
        }

        UserDTO user = userManager.getById(updateRequest.getUserId());
        CertificationBodyDTO acb = acbManager.getById(updateRequest.getAcbId());

        if (user == null || acb == null) {
            throw new InvalidArgumentsException("Could not find either ACB or User specified");
        }

        Permission permission = ChplPermission.toPermission(updateRequest.getAuthority());
        acbManager.addPermission(acb, updateRequest.getUserId(), permission);
        return "{\"userAdded\" : true}";
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Remove user permissions from an ACB.",
            notes = "The logged in user must have ROLE_ADMIN or ROLE_ACB and have administrative authority on the "
                    + " specified ACB. The user specified in the request will have all authorities "
                    + " removed that are associated with the specified ACB.")
    @RequestMapping(value = "{acbId}/remove_user/{userId}", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public String deleteUserFromAcbDeprecated(@PathVariable final Long acbId, @PathVariable final Long userId)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        return deleteUser(acbId, userId);
    }

    @ApiOperation(value = "Remove user permissions from an ACB.",
            notes = "The logged in user must have ROLE_ADMIN or ROLE_ACB and have administrative authority on the "
                    + " specified ACB. The user specified in the request will have all authorities "
                    + " removed that are associated with the specified ACB.")
    @RequestMapping(value = "{acbId}/users/{userId}", method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public String deleteUserFromAcb(@PathVariable final Long acbId, @PathVariable final Long userId)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        return deleteUser(acbId, userId);
    }

    private String deleteUser(final Long acbId, final Long userId)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        UserDTO user = userManager.getById(userId);
        CertificationBodyDTO acb = acbManager.getById(acbId);

        if (user == null || acb == null) {
            throw new InvalidArgumentsException("Could not find either ACB or User specified");
        }

        // delete all permissions on that acb
        acbManager.deleteAllPermissionsOnAcb(acb, new PrincipalSid(user.getSubjectName()));

        return "{\"userDeleted\" : true}";
    }

    @ApiOperation(value = "List users with permissions on a specified ACB.",
            notes = "The logged in user must have ROLE_ADMIN or have administrative or read authority on the "
                    + " specified ACB.")
    @RequestMapping(value = "/{acbId}/users", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody PermittedUserResults getUsers(@PathVariable("acbId") final Long acbId)
            throws InvalidArgumentsException, EntityRetrievalException {
        CertificationBodyDTO acb = acbManager.getById(acbId);
        if (acb == null) {
            throw new InvalidArgumentsException("Could not find the ACB specified.");
        }

        List<PermittedUser> acbUsers = new ArrayList<PermittedUser>();
        List<UserDTO> users = acbManager.getAllUsersOnAcb(acb);
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

                List<Permission> permissions = acbManager.getPermissionsForUser(acb,
                        new PrincipalSid(user.getSubjectName()));
                List<String> acbPerm = new ArrayList<String>(permissions.size());
                for (Permission permission : permissions) {
                    ChplPermission perm = ChplPermission.fromPermission(permission);
                    if (perm != null) {
                        acbPerm.add(perm.toString());
                    }
                }

                PermittedUser userInfo = new PermittedUser();
                userInfo.setUser(new User(user));
                userInfo.setPermissions(acbPerm);
                userInfo.setRoles(roleNames);
                acbUsers.add(userInfo);
            }
        }

        PermittedUserResults results = new PermittedUserResults();
        results.setUsers(acbUsers);
        return results;
    }
}
