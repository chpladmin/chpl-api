package gov.healthit.chpl.dao.auth;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.auth.UserPermissionDTO;
import gov.healthit.chpl.entity.auth.UserPermissionEntity;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;

@Repository(value = "userPermissionDAO")
public class UserPermissionDAO extends BaseDAOImpl {

    public List<UserPermissionDTO> findAll() {

        List<UserPermissionEntity> results = entityManager
                .createQuery("from UserPermissionEntity where (NOT deleted = true) ", UserPermissionEntity.class)
                .getResultList();
        List<UserPermissionDTO> permissions = new ArrayList<UserPermissionDTO>();

        for (UserPermissionEntity entity : results) {

            UserPermissionDTO permission = new UserPermissionDTO(entity);
            permission.setDescription(entity.getDescription());
            permission.setName(entity.getName());
            permissions.add(permission);
        }

        return permissions;
    }

    public UserPermissionDTO getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException {

        UserPermissionEntity permissionEntity = null;

        Query query = entityManager.createQuery(
                "from UserPermissionEntity where (NOT deleted = true) AND (authority = :authority) ",
                UserPermissionEntity.class);
        query.setParameter("authority", authority);
        List<UserPermissionEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new UserPermissionRetrievalException("Data error. Duplicate authority in database.");
        }

        if (result.size() > 0) {
            permissionEntity = result.get(0);
        } else {
            throw new UserPermissionRetrievalException("Permission does not exist.");
        }
        return new UserPermissionDTO(permissionEntity);
    }

    public Long getIdFromAuthority(String authority) throws UserPermissionRetrievalException {
        UserPermissionEntity permissionEntity = null;

        Query query = entityManager.createQuery(
                "from UserPermissionEntity where (NOT deleted = true) AND (authority = :authority) ",
                UserPermissionEntity.class);
        query.setParameter("authority", authority);
        List<UserPermissionEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new UserPermissionRetrievalException("Data error. Duplicate authority in database.");
        }

        if (result.size() > 0) {
            permissionEntity = result.get(0);
        } else {
            throw new UserPermissionRetrievalException("Permission does not exist.");
        }

        return permissionEntity.getId();
    }

    private UserPermissionEntity getById(Long permissionId) {

        Query query = entityManager.createQuery(
                "SELECT e FROM UserPermissionEntity e " + "WHERE user_permission_id = :permissionId",
                UserPermissionEntity.class);
        query.setParameter("permissionId", permissionId);

        List<UserPermissionEntity> results = query.getResultList();
        if (results == null || results.size() == 0) {
            return null;
        }
        return results.get(0);
    }

}
