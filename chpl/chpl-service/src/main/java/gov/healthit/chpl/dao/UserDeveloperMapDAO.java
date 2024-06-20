package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.entity.UserDeveloperMapEntity;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.entity.developer.DeveloperEntitySimple;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.UserMapper;
import jakarta.persistence.Query;

@Repository(value = "userDeveloperMapDAO")
public class UserDeveloperMapDAO extends BaseDAOImpl {

    private UserMapper userMapper;

    @Autowired
    public UserDeveloperMapDAO(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    public UserDeveloperMapDTO create(UserDeveloperMapDTO dto) throws EntityRetrievalException {
        UserDeveloperMapEntity entity = new UserDeveloperMapEntity();
        entity = create(getUserDeveloperMapEntity(dto));
        return mapEntityToDto(entity);
    }


    public void delete(final UserDeveloperMapDTO dto) throws EntityRetrievalException {
        UserDeveloperMapEntity entity = getEntityById(dto.getId());
        entity.setDeleted(true);
        update(entity);
    }


    public List<UserDeveloperMapDTO> getByUserId(Long userId) {
        Query query = entityManager.createQuery(
                "from UserDeveloperMapEntity udm "
                        + "join fetch udm.developer developer "
                        + "join fetch udm.user u "
                        + "join fetch u.permission perm "
                        + "join fetch u.contact contact "
                        + "where (udm.deleted != true) AND (u.id = :userId) ",
                UserDeveloperMapEntity.class);
        query.setParameter("userId", userId);
        List<UserDeveloperMapEntity> result = query.getResultList();

        List<UserDeveloperMapDTO> dtos = new ArrayList<UserDeveloperMapDTO>();
        if (result != null) {
            for (UserDeveloperMapEntity entity : result) {
                dtos.add(mapEntityToDto(entity));
            }
        }
        return dtos;
    }

    public List<UserDeveloperMapDTO> getByDeveloperId(Long developerId) {
        Query query = entityManager.createQuery(
                "from UserDeveloperMapEntity udm "
                        + "join fetch udm.developer developer "
                        + "join fetch udm.user u "
                        + "join fetch u.permission perm "
                        + "join fetch u.contact contact "
                        + "where (udm.deleted != true) "
                        + "AND (developer.id = :developerId) ",
                UserDeveloperMapEntity.class);
        query.setParameter("developerId", developerId);
        List<UserDeveloperMapEntity> result = query.getResultList();

        List<UserDeveloperMapDTO> dtos = new ArrayList<UserDeveloperMapDTO>();
        for (UserDeveloperMapEntity entity : result) {
            dtos.add(mapEntityToDto(entity));
        }
        return dtos;
    }

    public UserDeveloperMapDTO getById(Long id) {
        Query query = entityManager.createQuery(
                "from UserDeveloperMapEntity udm "
                        + "join fetch udm.developer developer "
                        + "join fetch udm.user u"
                        + "join fetch u.permission perm "
                        + "join fetch u.contact contact "
                        + "where (udm.deleted != true) AND (udm.id = :id) ",
                UserDeveloperMapEntity.class);
        query.setParameter("id", id);
        List<UserDeveloperMapEntity> result = query.getResultList();

        if (result.size() == 0) {
            return null;
        }
        return mapEntityToDto(result.get(0));
    }

    public List<UserDeveloperMapDTO> getAllDeveloperUsers() {
        Query query = entityManager.createQuery(
                "SELECT udm "
                        + "FROM UserDeveloperMapEntity udm "
                        + "JOIN FETCH udm.developer developer "
                        + "JOIN FETCH udm.user u "
                        + "JOIN FETCH u.permission perm "
                        + "JOIN FETCH u.contact contact "
                        + "WHERE (udm.deleted != true) ",
                UserDeveloperMapEntity.class);
        List<UserDeveloperMapEntity> entities = query.getResultList();
        return entities.stream()
                .map(entity -> mapEntityToDto(entity))
                .collect(Collectors.toList());
    }

    private UserDeveloperMapEntity getEntityById(final Long id) {
        Query query = entityManager.createQuery(
                "from UserDeveloperMapEntity udm "
                        + "join fetch udm.developer developer "
                        + "join fetch udm.user u "
                        + "join fetch u.permission perm "
                        + "join fetch u.contact contact "
                        + "where (udm.deleted != true) AND (udm.id = :id) ",
                UserDeveloperMapEntity.class);
        query.setParameter("id", id);
        List<UserDeveloperMapEntity> result = query.getResultList();

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private UserDeveloperMapEntity create(UserDeveloperMapEntity entity) {
        if (entity.getDeleted() == null) {
            entity.setDeleted(false);
        }
        entityManager.persist(entity);
        return entity;
    }

    private UserDeveloperMapEntity update(UserDeveloperMapEntity entity) {
        entityManager.persist(entity);
        return entity;
    }

    private DeveloperEntitySimple getDeveloperEntityById(final Long entityId) throws EntityRetrievalException {
        DeveloperEntitySimple entity = null;

        String queryStr = "SELECT developer from DeveloperEntitySimple developer "
                + "WHERE (developer.id = :entityid)" + " AND (developer.deleted = false)";

        Query query = entityManager.createQuery(queryStr, DeveloperEntitySimple.class);
        query.setParameter("entityid", entityId);
        List<DeveloperEntitySimple> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("developer.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate developer id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }
        return entity;
    }

    private UserEntity getUserEntityById(final Long userId) throws EntityRetrievalException {

        UserEntity user = null;

        Query query = entityManager.createQuery(
                "from UserEntity where (NOT deleted = true) " + "AND (id = :userid) ", UserEntity.class);
        query.setParameter("userid", userId);
        List<UserEntity> result = query.getResultList();

        if (result == null || result.size() == 0) {
            String msg = msgUtil.getMessage("user.notFound");
            throw new EntityRetrievalException(msg);
        } else if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate user id in database.");
        }

        if (result.size() == 0) {
            return null;
        }
        return result.get(0);
    }

    private UserDeveloperMapEntity getUserDeveloperMapEntity(final UserDeveloperMapDTO dto)
            throws EntityRetrievalException {
        UserDeveloperMapEntity entity = new UserDeveloperMapEntity();
        entity.setId(dto.getId());
        entity.setDeveloper(getDeveloperEntityById(dto.getDeveloper().getId()));
        entity.setUser(getUserEntityById(dto.getUser().getId()));
        return entity;
    }

    private UserDeveloperMapDTO mapEntityToDto(UserDeveloperMapEntity entity) {
        UserDeveloperMapDTO dto = new UserDeveloperMapDTO(entity);
        dto.setUser(userMapper.from(entity.getUser()));
        return dto;
    }

}
