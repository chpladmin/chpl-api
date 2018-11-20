package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.impl.UpdateTestingLabException;

public interface TestingLabManager {

    public void addPermission(TestingLabDTO atl, Long userId, Permission permission) throws UserRetrievalException;

    public void deletePermission(TestingLabDTO atl, Sid recipient, Permission permission);

    public void deleteAllPermissionsOnAtl(TestingLabDTO atl, Sid recipient);

    public void deletePermissionsForUser(UserDTO userDto) throws UserRetrievalException;

    public TestingLabDTO create(TestingLabDTO atl)
            throws UserRetrievalException, EntityCreationException, EntityRetrievalException, JsonProcessingException;

    public TestingLabDTO update(TestingLabDTO atl) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, UpdateTestingLabException;

    public TestingLabDTO retire(final Long atlId) throws EntityRetrievalException,
        JsonProcessingException, EntityCreationException, UpdateTestingLabException;

    public TestingLabDTO unretire(final Long atlId) throws EntityRetrievalException,
        JsonProcessingException, EntityCreationException, UpdateTestingLabException;

    public List<TestingLabDTO> getAllForUser();

    public List<TestingLabDTO> getAll();

    public TestingLabDTO getById(Long id) throws EntityRetrievalException;

    List<UserDTO> getAllUsersOnAtl(TestingLabDTO atl);

    List<Permission> getPermissionsForUser(TestingLabDTO atl, Sid recipient);
}
