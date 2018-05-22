package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ChplPermission;
import gov.healthit.chpl.domain.PermittedUser;
import gov.healthit.chpl.domain.TestingLab;
import gov.healthit.chpl.domain.UpdateUserAndAtlRequest;
import gov.healthit.chpl.dto.AddressDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.manager.TestingLabManager;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;
import gov.healthit.chpl.web.controller.results.PermittedUserResults;
import gov.healthit.chpl.web.controller.results.TestingLabResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "atls")
@RestController
@RequestMapping("/atls")
public class TestingLabController {

    @Autowired
    TestingLabManager atlManager;
    @Autowired
    UserManager userManager;

    private static final Logger LOGGER = LogManager.getLogger(TestingLabController.class);

    @ApiOperation(value = "List all testing labs (ATLs).",
            notes = "Setting the 'editable' parameter to true will return all ATLs that the logged in user has edit permissions on."
                    + "Setting 'showDeleted' to true will include even those ATLs that have been deleted. The logged in user must have ROLE_ADMIN "
                    + "to see deleted ATLs. The default behavior of this service is to list all of the ATLs in the system that are not deleted.")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody TestingLabResults getAtls(
            @RequestParam(required = false, defaultValue = "false") boolean editable,
            @RequestParam(required = false, defaultValue = "false") boolean showDeleted) {
        TestingLabResults results = new TestingLabResults();
        if (!Util.isUserRoleAdmin() && showDeleted) {
            throw new AccessDeniedException("Only Admin can see deleted ATL's.");
        } else {
            results = new TestingLabResults();
            List<TestingLabDTO> atls = null;
            if (editable) {
                atls = atlManager.getAllForUser(showDeleted);
            } else {
                atls = atlManager.getAll(showDeleted);
            }

            if (atls != null) {
                for (TestingLabDTO atl : atls) {
                    results.getAtls().add(new TestingLab(atl));
                }
            }
        }
        return results;
    }

    @ApiOperation(value = "Get details about a specific testing lab (ATL).",
            notes = "The logged in user must have ROLE_ADMIN or have either read or"
                    + "administrative authority on the testing lab with the ID specified.")
    @RequestMapping(value = "/{atlId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody TestingLab getAtlById(@PathVariable("atlId") Long atlId) throws EntityRetrievalException {
        TestingLabDTO atl = atlManager.getById(atlId);

        return new TestingLab(atl);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Create a new testing lab.",
            notes = "The logged in user must have ROLE_ADMIN to create a new testing lab.")
    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public TestingLab createAtlDeprecated(@RequestBody TestingLab atlInfo) throws InvalidArgumentsException, UserRetrievalException,
            EntityRetrievalException, EntityCreationException, JsonProcessingException {
        
        return create(atlInfo);
    }
    
    @ApiOperation(value = "Create a new testing lab.",
            notes = "The logged in user must have ROLE_ADMIN to create a new testing lab.")
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public TestingLab createAtl(@RequestBody TestingLab atlInfo) throws InvalidArgumentsException, UserRetrievalException,
            EntityRetrievalException, EntityCreationException, JsonProcessingException {
        
        return create(atlInfo);
    }

    public TestingLab create(TestingLab atlInfo) throws InvalidArgumentsException, UserRetrievalException,
                EntityRetrievalException, EntityCreationException, JsonProcessingException {
        TestingLabDTO toCreate = new TestingLabDTO();
        toCreate.setTestingLabCode(atlInfo.getAtlCode());
        toCreate.setAccredidationNumber(atlInfo.getAccredidationNumber());
        if (StringUtils.isEmpty(atlInfo.getName())) {
            throw new InvalidArgumentsException("A name is required for a testing lab");
        }
        toCreate.setName(atlInfo.getName());
        toCreate.setWebsite(atlInfo.getWebsite());
        
        if (atlInfo.getAddress() == null) {
            throw new InvalidArgumentsException("An address is required for a new testing lab");
        }
        AddressDTO address = new AddressDTO();
        address.setId(atlInfo.getAddress().getAddressId());
        address.setStreetLineOne(atlInfo.getAddress().getLine1());
        address.setStreetLineTwo(atlInfo.getAddress().getLine2());
        address.setCity(atlInfo.getAddress().getCity());
        address.setState(atlInfo.getAddress().getState());
        address.setZipcode(atlInfo.getAddress().getZipcode());
        address.setCountry(atlInfo.getAddress().getCountry());
        toCreate.setAddress(address);
        toCreate = atlManager.create(toCreate);
        return new TestingLab(toCreate);
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Update an existing ATL.",
            notes = "The logged in user must either have ROLE_ADMIN or have administrative "
                    + "authority on the testing lab whose data is being updated.")
    @RequestMapping(value = "/update", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ResponseEntity<TestingLab> updateAtlDeprecated(@RequestBody TestingLab atlInfo) throws InvalidArgumentsException,
            EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateTestingLabException {
        
        return update(atlInfo);
    }

    @ApiOperation(value = "Update an existing ATL.",
            notes = "The logged in user must either have ROLE_ADMIN or have administrative "
                    + "authority on the testing lab whose data is being updated.")
    @RequestMapping(value = "/{atlId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ResponseEntity<TestingLab> updateAtl(@RequestBody TestingLab atlInfo) throws InvalidArgumentsException,
            EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateTestingLabException {
        
        return update(atlInfo);
    }

    public ResponseEntity<TestingLab> update(TestingLab atlInfo) throws InvalidArgumentsException,
                EntityRetrievalException, JsonProcessingException, EntityCreationException, UpdateTestingLabException {
        TestingLabDTO toUpdate = new TestingLabDTO();
        toUpdate.setId(atlInfo.getId());
        toUpdate.setTestingLabCode(atlInfo.getAtlCode());
        toUpdate.setAccredidationNumber(atlInfo.getAccredidationNumber());
        if (StringUtils.isEmpty(atlInfo.getName())) {
            throw new InvalidArgumentsException("A name is required for a testing lab");
        }
        toUpdate.setName(atlInfo.getName());
        toUpdate.setWebsite(atlInfo.getWebsite());
        
        if (atlInfo.getAddress() == null) {
            throw new InvalidArgumentsException("An address is required to update the testing lab");
        }
        AddressDTO address = new AddressDTO();
        address.setId(atlInfo.getAddress().getAddressId());
        address.setStreetLineOne(atlInfo.getAddress().getLine1());
        address.setStreetLineTwo(atlInfo.getAddress().getLine2());
        address.setCity(atlInfo.getAddress().getCity());
        address.setState(atlInfo.getAddress().getState());
        address.setZipcode(atlInfo.getAddress().getZipcode());
        address.setCountry(atlInfo.getAddress().getCountry());
        toUpdate.setAddress(address);
        
        TestingLabDTO result = atlManager.update(toUpdate);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Cache-cleared", CacheNames.COLLECTIONS_LISTINGS);
        TestingLab response = new TestingLab(result);
        return new ResponseEntity<TestingLab>(response, responseHeaders, HttpStatus.OK);
    }
    
    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Delete an ATL.", notes = "The logged in user must have ROLE_ADMIN.")
    @RequestMapping(value = "/{atlId}/delete", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public String deleteAtlDeprecated(@PathVariable("atlId") Long atlId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        return delete(atlId);
    }

    @ApiOperation(value = "Delete an ATL.", notes = "The logged in user must have ROLE_ADMIN.")
    @RequestMapping(value = "/{atlId}", method = RequestMethod.DELETE,
            produces = "application/json; charset=utf-8")
    public String deleteAtl(@PathVariable("atlId") Long atlId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        return delete(atlId);
    }
    
    private String delete(Long atlId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException {

        TestingLabDTO toDelete = atlManager.getById(atlId);
        atlManager.delete(toDelete);
        return "{\"deletedAtl\" : true}";

    }

    @ApiOperation(value = "Restore a deleted ATL.",
            notes = "ATLs are unique in the CHPL in that they can be restored after a delete."
                    + " The logged in user must have ROLE_ADMIN.")
    @RequestMapping(value = "/{atlId}/undelete", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public String undeleteAtl(@PathVariable("atlId") Long atlId)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException {

        TestingLabDTO toResurrect = atlManager.getById(atlId, true);
        atlManager.undelete(toResurrect);
        return "{\"resurrectedAtl\" : true}";

    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Add a user to an ATL.",
            notes = "The logged in user must have ROLE_ADMIN or ROLE_ATL and have administrative authority on the "
                    + " specified ATL. It is recommended to pass 'ADMIN' in as the 'authority' field"
                    + " to guarantee maximum compatibility although 'READ' and 'DELETE' are also valid choices. "
                    + " Note that this method gives special permission on a specific ATL and is not the "
                    + " equivalent of assigning the ROLE_ATL role. Please view /users/grant_role "
                    + " request for more information on that.")
    @RequestMapping(value = "/add_user", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public String addUserToAtlDeprecated(@RequestBody UpdateUserAndAtlRequest updateRequest)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        return addUser(updateRequest);
    }

    @ApiOperation(value = "Add a user to an ATL.",
            notes = "The logged in user must have ROLE_ADMIN or ROLE_ATL and have administrative authority on the "
                    + " specified ATL. It is recommended to pass 'ADMIN' in as the 'authority' field"
                    + " to guarantee maximum compatibility although 'READ' and 'DELETE' are also valid choices. "
                    + " Note that this method gives special permission on a specific ATL and is not the "
                    + " equivalent of assigning the ROLE_ATL role. Please view /users/grant_role "
                    + " request for more information on that.")
    @RequestMapping(value = "/{atlId}/users", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public String addUserToAtl(@RequestBody UpdateUserAndAtlRequest updateRequest)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        return addUser(updateRequest);
    }

    private String addUser(UpdateUserAndAtlRequest updateRequest)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        if (updateRequest.getAtlId() == null || updateRequest.getUserId() == null || updateRequest.getUserId() <= 0
                || updateRequest.getAuthority() == null) {
            throw new InvalidArgumentsException("ATL ID, User ID (greater than 0), and Authority are required.");
        }

        UserDTO user = userManager.getById(updateRequest.getUserId());
        TestingLabDTO atl = atlManager.getById(updateRequest.getAtlId());

        if (user == null || atl == null) {
            throw new InvalidArgumentsException("Could not find either ATL or User specified");
        }

        Permission permission = ChplPermission.toPermission(updateRequest.getAuthority());
        atlManager.addPermission(atl, updateRequest.getUserId(), permission);
        return "{\"userAdded\" : true}";
    }

    @Deprecated
    @ApiOperation(value = "DEPRECATED.  Remove user permissions from an ATL.",
            notes = "The logged in user must have ROLE_ADMIN or ROLE_ATL and have administrative authority on the "
                    + " specified ATL. The user specified in the request will have all authorities "
                    + " removed that are associated with the specified ATL.")
    @RequestMapping(value = "{atlId}/remove_user/{userId}", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public String deleteUserFromAtlDeprecated(@PathVariable Long atlId, @PathVariable Long userId)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        return deleteUser(atlId, userId);
    }

    @ApiOperation(value = "Remove user permissions from an ATL.",
            notes = "The logged in user must have ROLE_ADMIN or ROLE_ATL and have administrative authority on the "
                    + " specified ATL. The user specified in the request will have all authorities "
                    + " removed that are associated with the specified ATL.")
    @RequestMapping(value = "{atlId}/users/{userId}", method = RequestMethod.DELETE,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public String deleteUserFromAtl(@PathVariable Long atlId, @PathVariable Long userId)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        return deleteUser(atlId, userId);
    }

    private String deleteUser(Long atlId, Long userId)
            throws UserRetrievalException, EntityRetrievalException, InvalidArgumentsException {

        UserDTO user = userManager.getById(userId);
        TestingLabDTO atl = atlManager.getById(atlId);

        if (user == null || atl == null) {
            throw new InvalidArgumentsException("Could not find either ATL or User specified");
        }

        // delete all permissions on that atl
        atlManager.deleteAllPermissionsOnAtl(atl, new PrincipalSid(user.getSubjectName()));

        return "{\"userDeleted\" : true}";
    }

    @ApiOperation(value = "List users with permissions on a specified ATL.",
            notes = "The logged in user must have ROLE_ADMIN or have administrative or read authority on the "
                    + " specified ATL.")
    @RequestMapping(value = "/{atlId}/users", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody PermittedUserResults getUsers(@PathVariable("atlId") Long atlId)
            throws InvalidArgumentsException, EntityRetrievalException {
        TestingLabDTO atl = atlManager.getById(atlId);
        if (atl == null) {
            throw new InvalidArgumentsException("Could not find the ATL specified.");
        }

        List<PermittedUser> atlUsers = new ArrayList<PermittedUser>();
        List<UserDTO> users = atlManager.getAllUsersOnAtl(atl);
        for (UserDTO user : users) {

            // only show users that have ROLE_ATL
            Set<UserPermissionDTO> systemPermissions = userManager.getGrantedPermissionsForUser(user);
            boolean hasAtlPermission = false;
            for (UserPermissionDTO systemPermission : systemPermissions) {
                if (systemPermission.getAuthority().equals("ROLE_ATL")) {
                    hasAtlPermission = true;
                }
            }

            if (hasAtlPermission) {
                List<String> roleNames = new ArrayList<String>();
                for (UserPermissionDTO role : systemPermissions) {
                    roleNames.add(role.getAuthority());
                }

                List<Permission> permissions = atlManager.getPermissionsForUser(atl,
                        new PrincipalSid(user.getSubjectName()));
                List<String> atlPerm = new ArrayList<String>(permissions.size());
                for (Permission permission : permissions) {
                    ChplPermission perm = ChplPermission.fromPermission(permission);
                    if (perm != null) {
                        atlPerm.add(perm.toString());
                    }
                }

                PermittedUser userInfo = new PermittedUser();
                userInfo.setUser(new User(user));
                userInfo.setPermissions(atlPerm);
                userInfo.setRoles(roleNames);
                atlUsers.add(userInfo);
            }
        }

        PermittedUserResults results = new PermittedUserResults();
        results.setUsers(atlUsers);
        return results;
    }
}
