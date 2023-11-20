package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.CHPLFileDAO;
import gov.healthit.chpl.dto.CHPLFileDTO;
import gov.healthit.chpl.dto.FileTypeDTO;
import gov.healthit.chpl.entity.CHPLFileEntity;
import gov.healthit.chpl.entity.FileTypeEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository(value = "filesDAO")
public class CHPLFileDAOImpl extends BaseDAOImpl implements CHPLFileDAO {

    @Override
    public CHPLFileEntity create(final CHPLFileDTO dto) throws EntityCreationException, EntityRetrievalException {
        FileTypeEntity type = new FileTypeEntity();
        type.setId(dto.getFileType().getId());

        CHPLFileEntity insert = new CHPLFileEntity();
        insert.setFileData(dto.getFileData());
        insert.setFileName(dto.getFileName());
        insert.setContentType(dto.getContentType());
        insert.setAssociatedDate(dto.getAssociatedDate());
        insert.setLastModifiedDate(new Date());
        insert.setDeleted(false);
        insert.setFileType(type);

        entityManager.persist(insert);
        entityManager.flush();
        return insert;
    }

    @Override
    public void delete(final Long id) throws EntityRetrievalException {
        CHPLFileEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            entityManager.merge(toDelete);
        }

    }

    @Override
    public List<CHPLFileDTO> findByFileType(final FileTypeDTO fileType) throws EntityRetrievalException {
        List<CHPLFileDTO> dtos = new ArrayList<CHPLFileDTO>();

        List<CHPLFileEntity> entities = getEntityByFileTypeId(fileType.getId());

        for (CHPLFileEntity entity : entities) {
            dtos.add(new CHPLFileDTO(entity));
        }
        return dtos;
    }

    @Override
    public CHPLFileDTO getById(final Long id) throws EntityRetrievalException {
        CHPLFileDTO dto = null;
        CHPLFileEntity file = this.getEntityById(id);

        if (file != null) {
            dto = new CHPLFileDTO(file);
        }
        return dto;
    }

    public CHPLFileEntity getEntityById(final Long id) throws EntityRetrievalException {
        CHPLFileEntity entity = null;

        Query query = entityManager.createQuery(
                "FROM CHPLFileEntity f "
                + "LEFT OUTER JOIN FETCH f.fileType "
                + "WHERE (NOT f.deleted = true) "
                + "AND (f.id = :entityid) ", CHPLFileEntity.class);
        query.setParameter("entityid", id);
        List<CHPLFileEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate file id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    public List<CHPLFileEntity> getEntityByFileTypeId(final Long fileTypeId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "FROM CHPLFileEntity f "
                + "LEFT OUTER JOIN FETCH f.fileType ft "
                + "WHERE (NOT f.deleted = true) "
                + "AND (ft.id = :entityid) ", CHPLFileEntity.class);
        query.setParameter("entityid", fileTypeId);
        List<CHPLFileEntity> result = query.getResultList();

        return result;
    }
}
