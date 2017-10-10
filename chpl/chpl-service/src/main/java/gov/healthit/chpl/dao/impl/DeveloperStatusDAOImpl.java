package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.DeveloperStatusDAO;
import gov.healthit.chpl.dto.DeveloperStatusDTO;
import gov.healthit.chpl.entity.developer.DeveloperStatusEntity;
import gov.healthit.chpl.entity.developer.DeveloperStatusType;

@Repository("developerStatusDAO")
public class DeveloperStatusDAOImpl extends BaseDAOImpl implements DeveloperStatusDAO {

    @Override
    public DeveloperStatusDTO getById(Long id) {

        DeveloperStatusDTO dto = null;
        DeveloperStatusEntity entity = getEntityById(id);

        if (entity != null) {
            dto = new DeveloperStatusDTO(entity);
        }
        return dto;
    }

    @Override
    public DeveloperStatusDTO getByName(String name) {

        DeveloperStatusDTO dto = null;
        List<DeveloperStatusEntity> entities = getEntitiesByName(name);

        if (entities != null && entities.size() > 0) {
            dto = new DeveloperStatusDTO(entities.get(0));
        }
        return dto;
    }

    @Override
    public List<DeveloperStatusDTO> findAll() {

        List<DeveloperStatusEntity> entities = getAllEntities();
        List<DeveloperStatusDTO> dtos = new ArrayList<DeveloperStatusDTO>();

        for (DeveloperStatusEntity entity : entities) {
            DeveloperStatusDTO dto = new DeveloperStatusDTO(entity);
            dtos.add(dto);
        }
        return dtos;

    }

    List<DeveloperStatusEntity> getAllEntities() {
        return entityManager
                .createQuery("from DeveloperStatusEntity where (NOT deleted = true) ", DeveloperStatusEntity.class)
                .getResultList();
    }

    DeveloperStatusEntity getEntityById(Long id) {

        DeveloperStatusEntity entity = null;

        Query query = entityManager.createQuery(
                "from DeveloperStatusEntity ds where (NOT deleted = true) AND (ds.id = :entityid) ",
                DeveloperStatusEntity.class);
        query.setParameter("entityid", id);
        List<DeveloperStatusEntity> result = query.getResultList();
        if (result.size() > 0) {
            entity = result.get(0);
        }

        return entity;
    }

    List<DeveloperStatusEntity> getEntitiesByName(String name) {

        Query query = entityManager.createQuery(
                "from DeveloperStatusEntity where " + "(NOT deleted = true) AND (name LIKE :name) ",
                DeveloperStatusEntity.class);
        query.setParameter("name", DeveloperStatusType.getValue(name));
        List<DeveloperStatusEntity> result = query.getResultList();

        return result;
    }
}
