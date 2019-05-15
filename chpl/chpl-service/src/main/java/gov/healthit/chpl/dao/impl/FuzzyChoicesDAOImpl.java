package gov.healthit.chpl.dao.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.dao.FuzzyChoicesDAO;
import gov.healthit.chpl.dto.FuzzyChoicesDTO;
import gov.healthit.chpl.entity.FuzzyChoicesEntity;
import gov.healthit.chpl.entity.FuzzyType;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.AuthUtil;

@Repository(value = "fuzzyChoicesDAO")
public class FuzzyChoicesDAOImpl extends BaseDAOImpl implements FuzzyChoicesDAO {

    @Transactional
    public FuzzyChoicesDTO create(FuzzyChoicesDTO dto)
        throws EntityRetrievalException, EntityCreationException, JsonParseException, JsonMappingException, IOException {
        FuzzyChoicesEntity entity = new FuzzyChoicesEntity();
        String toJSON = new ObjectMapper().writeValueAsString(dto.getChoices());
        entity.setChoices(toJSON);
        entity.setFuzzyType(dto.getFuzzyType());
        entity.setCreationDate(new Date());
        entity.setDeleted(false);
        entity.setLastModifiedDate(new Date());
        entity.setLastModifiedUser(AuthUtil.getAuditId());
        create(entity);
        return new FuzzyChoicesDTO(entity);
    }

    public FuzzyChoicesDTO getByType(FuzzyType fuzzy) throws EntityRetrievalException, JsonParseException, JsonMappingException, IOException {
        FuzzyChoicesEntity entity = getEntityByType(fuzzy);

        FuzzyChoicesDTO dto = null;
        if (entity != null) {
            dto = new FuzzyChoicesDTO(entity);
        }
        return dto;
    }

    @Override
    @Transactional
    public List<FuzzyChoicesDTO> findAllTypes() throws EntityRetrievalException, JsonParseException, JsonMappingException, IOException {
        List<FuzzyChoicesEntity> entities = entityManager
            .createQuery("SELECT fuzzy FROM FuzzyChoicesEntity fuzzy WHERE (fuzzy.deleted <> true) ",
                         FuzzyChoicesEntity.class)
            .getResultList();

        List<FuzzyChoicesDTO> dtos = new ArrayList<FuzzyChoicesDTO>();
        for (FuzzyChoicesEntity entity : entities) {
            FuzzyChoicesDTO dto = new FuzzyChoicesDTO(entity);
            dtos.add(dto);
        }
        return dtos;
    }

    @Transactional
    public FuzzyChoicesDTO update(FuzzyChoicesDTO dto)
        throws EntityRetrievalException, EntityCreationException, JsonParseException, JsonMappingException, IOException {
        FuzzyChoicesEntity entity = getEntityByType(dto.getFuzzyType());
        if (entity == null) {
            throw new EntityRetrievalException("Cannot update entity with type " + dto.getFuzzyType() + ". Entity does not exist.");
        }

        String toJSON = new ObjectMapper().writeValueAsString(dto.getChoices());
        entity.setChoices(toJSON);
        entity.setLastModifiedUser(AuthUtil.getAuditId());

        update(entity);
        return new FuzzyChoicesDTO(entity);
    }

    private FuzzyChoicesEntity getEntityByType(FuzzyType type)
        throws EntityRetrievalException {

        FuzzyChoicesEntity entity = null;

        String queryStr = "SELECT fuzzy from FuzzyChoicesEntity fuzzy where "
            + "(fuzzy_choices_id = :entityId)";

        Query query = entityManager.createQuery(queryStr, FuzzyChoicesEntity.class);
        query.setParameter("entityId", (type.ordinal() + 1));
        List<FuzzyChoicesEntity> result = query.getResultList();

        if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    private void create(FuzzyChoicesEntity announcement) {

        entityManager.persist(announcement);
        entityManager.flush();
    }

    private void update(FuzzyChoicesEntity announcement) {

        entityManager.merge(announcement);
        entityManager.flush();

    }
}
