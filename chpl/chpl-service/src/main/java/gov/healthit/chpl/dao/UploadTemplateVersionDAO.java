package gov.healthit.chpl.dao;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;
import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import gov.healthit.chpl.entity.listing.pending.UploadTemplateVersionEntity;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository("uploadTemplateVersionDao")
public class UploadTemplateVersionDAO extends BaseDAOImpl {

    /**
     * Get an upload template by ID.
     * @param id the ID of the template
     * @return the template
     * @throws EntityRetrievalException if no template exists with this ID
     */
    @Transactional
    public UploadTemplateVersionDTO getById(final Long id) throws EntityRetrievalException {
        UploadTemplateVersionEntity entity = getEntityById(id);
        UploadTemplateVersionDTO result = null;
        if (entity != null) {
            result = new UploadTemplateVersionDTO(entity);
        }
        return result;
    }

    /**
     * Get all available upload templates.
     * @return a list of upload templates.
     */
    @Transactional
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

    /**
     * Mark the template with the given ID as deleted.
     * @param id the ID of the template to mark deleted.
     * @throws EntityRetrievalException
     */
    @Transactional
    public void delete(final Long id) throws EntityRetrievalException {
        UploadTemplateVersionEntity entityToUpdate = getEntityById(id);
        entityToUpdate.setDeleted(true);
        update(entityToUpdate);
    }

    private UploadTemplateVersionEntity getEntityById(final Long id) throws EntityRetrievalException {
        Query query = entityManager.createQuery("SELECT entity "
                + "FROM UploadTemplateVersionEntity entity "
                + "WHERE (NOT entity.deleted = true) "
                + "AND entity.id = :id", UploadTemplateVersionEntity.class);
        query.setParameter("id", id);

        List<UploadTemplateVersionEntity> entities = query.getResultList();
        if (entities == null || entities.size() == 0) {
            String msg = msgUtil.getMessage("uploadTemplateVersion.notFound");
            throw new EntityRetrievalException(msg);
        }
        return entities.get(0);
    }
}
