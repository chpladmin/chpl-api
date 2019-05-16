package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.UploadTemplateVersionDAO;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import gov.healthit.chpl.entity.UcdProcessEntity;
import gov.healthit.chpl.entity.listing.pending.UploadTemplateVersionEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("uploadTemplateVersionDao")
public class UploadTemplateVersionDAOImpl extends BaseDAOImpl implements UploadTemplateVersionDAO {

    @Override
    public UploadTemplateVersionDTO getById(Long id) throws EntityRetrievalException {

        UploadTemplateVersionDTO dto = null;
        Query query = entityManager.createQuery("SELECT entity "
                + "FROM UploadTemplateVersionEntity entity "
                + "WHERE (NOT entity.deleted = true) "
                + "AND entity.id = :id", UploadTemplateVersionEntity.class);
        query.setParameter("id", id);

        List<UploadTemplateVersionEntity> entities = query.getResultList();
        if (entities == null || entities.size() == 0) {
            String msg = msgUtil.getMessage("uploadTemplateVersion.notFound");
            throw new EntityRetrievalException(msg);
        } else {
            dto = new UploadTemplateVersionDTO(entities.get(0));
        }
        return dto;
    }

    @Override
    public List<UploadTemplateVersionDTO> findAll() {

        List<UploadTemplateVersionEntity> entities =
                entityManager.createQuery("SELECT entity "
                        + "FROM UploadTemplateVersionEntity entity "
                        + "WHERE (NOT entity.deleted = true) ", UploadTemplateVersionEntity.class)
                .getResultList();
        List<UploadTemplateVersionDTO> dtos = new ArrayList<UploadTemplateVersionDTO>();

        for (UploadTemplateVersionEntity entity : entities) {
            UploadTemplateVersionDTO dto = new UploadTemplateVersionDTO(entity);
            dtos.add(dto);
        }
        return dtos;

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
