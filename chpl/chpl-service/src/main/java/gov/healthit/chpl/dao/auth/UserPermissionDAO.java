package gov.healthit.chpl.dao.auth;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.entity.auth.UserPermissionEntity;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;

@Repository(value = "userPermissionDAO")
public class UserPermissionDAO extends BaseDAOImpl {

    public List<UserPermission> findAll() {
        List<UserPermissionEntity> results = entityManager
                .createQuery("from UserPermissionEntity where (NOT deleted = true) ", UserPermissionEntity.class)
                .getResultList();
        return results.stream()
                    .map(entity -> entity.toDomain())
                    .collect(Collectors.toList());
    }

    public UserPermission getPermissionFromAuthority(String authority) throws UserPermissionRetrievalException {
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
        return permissionEntity.toDomain();
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
}
