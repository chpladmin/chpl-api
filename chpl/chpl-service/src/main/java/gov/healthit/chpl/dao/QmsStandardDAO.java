package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.entity.QmsStandardEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("qmsStandardDao")
public class QmsStandardDAO extends BaseDAOImpl {

    public QmsStandardDTO update(QmsStandardDTO dto) throws EntityRetrievalException {
        QmsStandardEntity entity = this.getEntityById(dto.getId());
        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }

        entity.setName(dto.getName());
        update(entity);
        return new QmsStandardDTO(entity);
    }

    public void delete(Long id) throws EntityRetrievalException {
        QmsStandardEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            update(toDelete);
        }
    }

    public QmsStandardDTO getById(Long id) {
        QmsStandardDTO dto = null;
        QmsStandardEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new QmsStandardDTO(entity);
        }
        return dto;
    }

    public QmsStandardDTO getByName(String name) {
        QmsStandardDTO dto = null;
        List<QmsStandardEntity> entities = getEntitiesByName(name);

        if (entities != null && entities.size() > 0) {
            dto = new QmsStandardDTO(entities.get(0));
        }
        return dto;
    }

    public List<QmsStandardDTO> findAll() {
        List<QmsStandardEntity> entities = getAllEntities();
        List<QmsStandardDTO> dtos = new ArrayList<QmsStandardDTO>();

        for (QmsStandardEntity entity : entities) {
            QmsStandardDTO dto = new QmsStandardDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    public QmsStandardDTO findOrCreate(Long id, String name) {
        QmsStandardDTO result = null;
        if (id != null) {
            result = getById(id);
        } else if (!StringUtils.isEmpty(name)) {
            result = getByName(name);
        }

        if (result == null) {
            QmsStandardDTO toCreate = new QmsStandardDTO();
            toCreate.setName(name.trim());
            result = create(toCreate);
        }
        return result;
    }

    private QmsStandardDTO create(QmsStandardDTO dto) {
        QmsStandardEntity entity = new QmsStandardEntity();
        entity.setCreationDate(new Date());
        entity.setDeleted(false);
        entity.setLastModifiedDate(new Date());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setName(dto.getName());
        create(entity);
        return new QmsStandardDTO(entity);
    }

    private List<QmsStandardEntity> getAllEntities() {
        return entityManager.createQuery("from QmsStandardEntity where (NOT deleted = true) ", QmsStandardEntity.class)
                .getResultList();
    }

    private QmsStandardEntity getEntityById(Long id) {
        QmsStandardEntity entity = null;

        Query query = entityManager.createQuery(
                "from QmsStandardEntity where (NOT deleted = true) AND (id = :entityid) ", QmsStandardEntity.class);
        query.setParameter("entityid", id);
        List<QmsStandardEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<QmsStandardEntity> getEntitiesByName(String name) {
        Query query = entityManager.createQuery(
                "from QmsStandardEntity where " + "(NOT deleted = true) AND (UPPER(name) = :name) ",
                QmsStandardEntity.class);
        query.setParameter("name", name.toUpperCase().trim());
        List<QmsStandardEntity> result = query.getResultList();

        return result;
    }

}
