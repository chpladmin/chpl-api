package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.entity.AccessibilityStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Repository("accessibilityStandardDAO")
@Log4j2
public class AccessibilityStandardDAO extends BaseDAOImpl {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public AccessibilityStandardDAO(ErrorMessageUtil msgUtil) {
        this.msgUtil = msgUtil;
    }

    public AccessibilityStandardDTO create(AccessibilityStandardDTO dto) throws EntityCreationException {
        AccessibilityStandardEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            try {
                entity = new AccessibilityStandardEntity();
                entity.setCreationDate(new Date());
                entity.setDeleted(false);
                entity.setLastModifiedDate(new Date());
                entity.setLastModifiedUser(AuthUtil.getAuditId());
                entity.setName(dto.getName());
                create(entity);
            } catch (Exception ex) {
                String msg = msgUtil.getMessage("listing.badAccessibilityStandard", dto.getName());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
            return new AccessibilityStandardDTO(entity);
        }
    }

    public AccessibilityStandardDTO update(AccessibilityStandardDTO dto) throws EntityRetrievalException {
        AccessibilityStandardEntity entity = this.getEntityById(dto.getId());
        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }
        entity.setName(dto.getName());
        update(entity);
        return new AccessibilityStandardDTO(entity);
    }

    public void delete(Long id) throws EntityRetrievalException {
        AccessibilityStandardEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }

    public AccessibilityStandardDTO getById(Long id) {
        AccessibilityStandardDTO dto = null;
        AccessibilityStandardEntity entity = getEntityById(id);
        if (entity != null) {
            dto = new AccessibilityStandardDTO(entity);
        }
        return dto;
    }

    public AccessibilityStandardDTO getByName(String name) {
        AccessibilityStandardDTO dto = null;
        List<AccessibilityStandardEntity> entities = getEntitiesByName(name);
        if (entities != null && entities.size() > 0) {
            dto = new AccessibilityStandardDTO(entities.get(0));
        }
        return dto;
    }

    public List<AccessibilityStandardDTO> findAll() {
        List<AccessibilityStandardEntity> entities = getAllEntities();
        List<AccessibilityStandardDTO> dtos = new ArrayList<AccessibilityStandardDTO>();

        for (AccessibilityStandardEntity entity : entities) {
            AccessibilityStandardDTO dto = new AccessibilityStandardDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    public AccessibilityStandardDTO findOrCreate(Long id, String name) throws EntityCreationException {
        AccessibilityStandardDTO result = null;
        if (id != null) {
            result = getById(id);
        } else if (!StringUtils.isEmpty(name)) {
            result = getByName(name);
        }

        if (result == null) {
            AccessibilityStandardDTO toCreate = new AccessibilityStandardDTO();
            toCreate.setName(name.trim());
            result = create(toCreate);
        }
        return result;
    }

    private List<AccessibilityStandardEntity> getAllEntities() {
        return entityManager.createQuery("from AccessibilityStandardEntity where (NOT deleted = true) ",
                AccessibilityStandardEntity.class).getResultList();
    }

    private AccessibilityStandardEntity getEntityById(Long id) {
        AccessibilityStandardEntity entity = null;
        Query query = entityManager.createQuery(
                "from AccessibilityStandardEntity where (NOT deleted = true) AND (id = :entityid) ",
                AccessibilityStandardEntity.class);
        query.setParameter("entityid", id);
        List<AccessibilityStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }
        return entity;
    }

    private List<AccessibilityStandardEntity> getEntitiesByName(String name) {
        Query query = entityManager.createQuery(
                "from AccessibilityStandardEntity where (NOT deleted = true) AND (name = :name) ",
                AccessibilityStandardEntity.class);
        query.setParameter("name", name);
        List<AccessibilityStandardEntity> result = query.getResultList();

        return result;
    }
}
