package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.UserRoleMapDTO;

public interface UserRoleMapDAO {
    List<UserRoleMapDTO> getByUserId(Long userId);
}
