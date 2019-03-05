package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.UserRoleMapDAO;
import gov.healthit.chpl.dto.UserRoleMapDTO;
import gov.healthit.chpl.entity.UserRoleMapEntity;

@Repository(value = "userRoleMapDAOImpl")
public class UserRoleMapDAOImpl extends BaseDAOImpl implements UserRoleMapDAO {

    @Override
    public List<UserRoleMapDTO> getByUserId(Long userId) {
        Query query = entityManager.createQuery("from UserRoleMapEntity urm " + "join fetch urm.role r "
                + "join fetch urm.user u " + "where (urm.deleted != true) AND (u.id = :userId) ",
                UserRoleMapEntity.class);
        query.setParameter("userId", userId);
        List<UserRoleMapEntity> result = query.getResultList();

        List<UserRoleMapDTO> dtos = new ArrayList<UserRoleMapDTO>();
        for (UserRoleMapEntity entity : result) {
            dtos.add(new UserRoleMapDTO(entity));
        }
        return dtos;
    }

}
