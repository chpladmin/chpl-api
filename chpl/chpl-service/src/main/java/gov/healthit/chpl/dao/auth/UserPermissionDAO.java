package gov.healthit.chpl.dao.auth;

import java.util.List;

import gov.healthit.chpl.dto.auth.UserPermissionDTO;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;

public interface UserPermissionDAO {
    public UserPermissionDTO getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException;
    public Long getIdFromAuthority(String authority) throws UserPermissionRetrievalException;
    public List<UserPermissionDTO> findAll();
    public UserPermissionDTO findById(Long id);
}
