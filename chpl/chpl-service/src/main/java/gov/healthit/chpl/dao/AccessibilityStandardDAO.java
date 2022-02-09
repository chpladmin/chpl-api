package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.AccessibilityStandardDTO;
import gov.healthit.chpl.entity.AccessibilityStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("accessibilityStandardDAO")
public class AccessibilityStandardDAO extends BaseDAOImpl {

    public AccessibilityStandardDTO create(AccessibilityStandardDTO dto) throws EntityCreationException {
        try {
            AccessibilityStandardEntity entity = new AccessibilityStandardEntity();
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            entity.setName(dto.getName());
            create(entity);
            return new AccessibilityStandardDTO(entity);
        } catch (Exception ex) {
            throw new EntityCreationException(ex);
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
