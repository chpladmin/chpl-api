package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.entity.TargetedUserEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Repository("targetedUserDao")
@Log4j2
public class TargetedUserDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TargetedUserDAO(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public Long create(String name) {
        TargetedUserEntity entity = new TargetedUserEntity();
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setName(name);
        create(entity);
        return entity.getId();
    }

    public TargetedUserDTO create(TargetedUserDTO dto) throws EntityCreationException {
        TargetedUserEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            try {
                entity = new TargetedUserEntity();
                entity.setCreationDate(new Date());
                entity.setDeleted(false);
                entity.setLastModifiedDate(new Date());
                entity.setLastModifiedUser(AuthUtil.getAuditId());
                entity.setName(dto.getName());
                create(entity);
            } catch (Exception ex) {
                String msg = msgUtil.getMessage("listing.badTargetedUser", dto.getName());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
            return new TargetedUserDTO(entity);
        }
    }

    public TargetedUserDTO update(TargetedUserDTO dto) throws EntityRetrievalException {
        TargetedUserEntity entity = this.getEntityById(dto.getId());
        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }
        entity.setName(dto.getName());
        update(entity);
        return new TargetedUserDTO(entity);
    }

    public void delete(Long id) throws EntityRetrievalException {
        TargetedUserEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }

    public TargetedUserDTO getById(Long id) {
        TargetedUserDTO dto = null;
        TargetedUserEntity entity = getEntityById(id);
        if (entity != null) {
            dto = new TargetedUserDTO(entity);
        }
        return dto;
    }

    public TargetedUserDTO getByName(String name) {
        TargetedUserDTO dto = null;
        List<TargetedUserEntity> entities = getEntitiesByName(name);

        if (entities != null && entities.size() > 0) {
            dto = new TargetedUserDTO(entities.get(0));
        }
        return dto;
    }

    public List<TargetedUserDTO> findAll() {
        List<TargetedUserEntity> entities = getAllEntities();
        List<TargetedUserDTO> dtos = new ArrayList<TargetedUserDTO>();

        for (TargetedUserEntity entity : entities) {
            TargetedUserDTO dto = new TargetedUserDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    public TargetedUserDTO findOrCreate(Long id, String name) throws EntityCreationException {
        TargetedUserDTO result = null;
        if (id != null) {
            result = getById(id);
        } else if (!StringUtils.isEmpty(name)) {
            result = getByName(name);
        }

        if (result == null) {
            TargetedUserDTO toCreate = new TargetedUserDTO();
            toCreate.setName(name.trim());
            result = create(toCreate);
        }
        return result;
    }

    private List<TargetedUserEntity> getAllEntities() {
        return entityManager
                .createQuery("from TargetedUserEntity where (NOT deleted = true) ", TargetedUserEntity.class)
                .getResultList();
    }

    private TargetedUserEntity getEntityById(Long id) {

        TargetedUserEntity entity = null;

        Query query = entityManager.createQuery(
                "from TargetedUserEntity where (NOT deleted = true) AND (id = :entityid) ", TargetedUserEntity.class);
        query.setParameter("entityid", id);
        List<TargetedUserEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<TargetedUserEntity> getEntitiesByName(String name) {

        Query query = entityManager.createQuery(
                "from TargetedUserEntity where " + "(NOT deleted = true) AND (UPPER(name) = :name) ",
                TargetedUserEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<TargetedUserEntity> result = query.getResultList();
        return result;
    }

}
