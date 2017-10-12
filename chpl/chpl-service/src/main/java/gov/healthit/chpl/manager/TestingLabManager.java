package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;

public interface TestingLabManager {

    void addPermission(TestingLabDTO atl, Long userId, Permission permission) throws UserRetrievalException;

    void deletePermission(TestingLabDTO atl, Sid recipient, Permission permission);

    void deleteAllPermissionsOnAtl(TestingLabDTO atl, Sid recipient);

    void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException;

    TestingLabDTO create(TestingLabDTO atl)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;

    TestingLabDTO update(TestingLabDTO atl) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException;

    void undelete(TestingLabDTO atl)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException;

    void delete(TestingLabDTO atl)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException, UserRetrievalException;

    List<TestingLabDTO> getAllForUser(boolean showDeleted);

    List<TestingLabDTO> getAll(boolean showDeleted);

    TestingLabDTO getById(Long id) throws EntityRetrievalException;

    TestingLabDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException;

    List<UserDTO> getAllUsersOnAtl(TestingLabDTO atl);

    List<Permission> getPermissionsForUser(TestingLabDTO atl, Sid recipient);
}
