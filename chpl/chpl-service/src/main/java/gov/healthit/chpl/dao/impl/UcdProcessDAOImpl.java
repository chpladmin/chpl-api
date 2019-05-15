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

import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.UcdProcessEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository("ucdProcessDAO")
public class UcdProcessDAOImpl extends BaseDAOImpl implements UcdProcessDAO {
    private static final Logger LOGGER = LogManager.getLogger(UcdProcessDAOImpl.class);
    @Autowired
    MessageSource messageSource;


    @Override
    public UcdProcessDTO update(UcdProcessDTO dto) throws EntityRetrievalException {
        UcdProcessEntity entity = this.getEntityById(dto.getId());

        if (entity == null) {
            throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
        }

        entity.setName(dto.getName());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        entity.setLastModifiedDate(new Date());

        entityManager.merge(entity);
        entityManager.flush();
        return new UcdProcessDTO(entity);
    }

    @Override
    public void delete(Long id) throws EntityRetrievalException {

        UcdProcessEntity toDelete = getEntityById(id);

        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            toDelete.setLastModifiedUser(AuthUtil.getAuditId());
            entityManager.merge(toDelete);
            entityManager.flush();
        }
    }

    @Override
    public UcdProcessDTO getById(Long id) {

        UcdProcessDTO dto = null;
        UcdProcessEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new UcdProcessDTO(entity);
        }
        return dto;
    }

    @Override
    public UcdProcessDTO getByName(String name) {

        UcdProcessDTO dto = null;
        List<UcdProcessEntity> entities = getEntitiesByName(name);

        if (entities != null && entities.size() > 0) {
            dto = new UcdProcessDTO(entities.get(0));
        }
        return dto;
    }

    @Override
    public UcdProcessDTO findOrCreate(Long id, String name) throws EntityCreationException {
        UcdProcessDTO result = null;
        if (id != null) {
            result = getById(id);
        } else if (!StringUtils.isEmpty(name)) {
            result = getByName(name);
        }

        if (result == null) {
            UcdProcessDTO toCreate = new UcdProcessDTO();
            toCreate.setName(name.trim());
            result = create(toCreate);
        }
        return result;
    }

    @Override
    public List<UcdProcessDTO> findAll() {

        List<UcdProcessEntity> entities = getAllEntities();
        List<UcdProcessDTO> dtos = new ArrayList<UcdProcessDTO>();

        for (UcdProcessEntity entity : entities) {
            UcdProcessDTO dto = new UcdProcessDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    private UcdProcessDTO create(UcdProcessDTO dto) throws EntityCreationException {
        UcdProcessEntity entity = null;
        if (dto.getId() != null) {
            entity = this.getEntityById(dto.getId());
        }

        if (entity != null) {
            throw new EntityCreationException("An entity with this ID already exists.");
        } else {
            entity = new UcdProcessEntity();
            entity.setCreationDate(new Date());
            entity.setDeleted(false);
            entity.setLastModifiedDate(new Date());
            entity.setLastModifiedUser(AuthUtil.getAuditId());
            entity.setName(dto.getName());

            try {
                entityManager.persist(entity);
                entityManager.flush();
            } catch (Exception ex) {
                String msg = String.format(
                        messageSource.getMessage(new DefaultMessageSourceResolvable("listing.criteria.badUcdProcess"),
                                LocaleContextHolder.getLocale()),
                        dto.getName());
                LOGGER.error(msg, ex);
                throw new EntityCreationException(msg);
            }
            return new UcdProcessDTO(entity);
        }
    }

    private List<UcdProcessEntity> getAllEntities() {
        return entityManager.createQuery("from UcdProcessEntity where (NOT deleted = true) ", UcdProcessEntity.class)
                .getResultList();
    }

    private UcdProcessEntity getEntityById(Long id) {

        UcdProcessEntity entity = null;

        Query query = entityManager.createQuery(
                "from UcdProcessEntity where (NOT deleted = true) AND (ucd_process_id = :entityid) ",
                UcdProcessEntity.class);
        query.setParameter("entityid", id);
        List<UcdProcessEntity> result = query.getResultList();

        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    private List<UcdProcessEntity> getEntitiesByName(String name) {

        Query query = entityManager.createQuery(
                "from UcdProcessEntity where " + "(NOT deleted = true) AND (UPPER(name) = :name) ",
                UcdProcessEntity.class);
        query.setParameter("name", name.toUpperCase());
        List<UcdProcessEntity> result = query.getResultList();

        return result;
    }
}
