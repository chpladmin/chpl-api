package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.QmsStandardDAO;
import gov.healthit.chpl.dto.QmsStandardDTO;
import gov.healthit.chpl.entity.QmsStandardEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("qmsStandardDao")
public class QmsStandardDAOImpl extends BaseDAOImpl implements QmsStandardDAO {
    private static final Logger LOGGER = LogManager.getLogger(QmsStandardDAOImpl.class);
    @Autowired
    MessageSource messageSource;

    @Override
    public QmsStandardDTO update(QmsStandardDTO dto) throws EntityRetrievalException {
        QmsStandardEntity entity = this.getEntityById(dto.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }

        entity.setName(dto.getName());
        entityManager.merge(entity);
        entityManager.flush();
        return new QmsStandardDTO(entity);
    }

    @Override
    public void delete(Long id) throws EntityRetrievalException {

        QmsStandardEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
            entityManager.merge(toDelete);
            entityManager.flush();
        }
    }

    @Override
    public QmsStandardDTO getById(Long id) {
        QmsStandardDTO dto = null;
        QmsStandardEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new QmsStandardDTO(entity);
        }
        return dto;
    }

    @Override
    public QmsStandardDTO getByName(String name) {

        QmsStandardDTO dto = null;
        List<QmsStandardEntity> entities = getEntitiesByName(name);

        if (entities != null && entities.size() > 0) {
            dto = new QmsStandardDTO(entities.get(0));
        }
        return dto;
    }

    @Override
    public List<QmsStandardDTO> findAll() {

        List<QmsStandardEntity> entities = getAllEntities();
        List<QmsStandardDTO> dtos = new ArrayList<QmsStandardDTO>();

        for (QmsStandardEntity entity : entities) {
            QmsStandardDTO dto = new QmsStandardDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    @Override
    public QmsStandardDTO findOrCreate(Long id, String name) throws EntityCreationException {
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

    private QmsStandardDTO create(QmsStandardDTO dto) throws EntityCreationException {

        QmsStandardEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            try {
                entity = new QmsStandardEntity();
                entity.setCreationDate(new Date());
                entity.setDeleted(false);
                entity.setLastModifiedDate(new Date());
                entity.setLastModifiedUser(Util.getCurrentUser().getId());
                entity.setName(dto.getName());
                entityManager.persist(entity);
                entityManager.flush();
            } catch (Exception ex) {
                String msg = String
                        .format(messageSource.getMessage(new DefaultMessageSourceResolvable("listing.badQmsStandard"),
                                LocaleContextHolder.getLocale()), dto.getName());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
            return new QmsStandardDTO(entity);
        }
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
