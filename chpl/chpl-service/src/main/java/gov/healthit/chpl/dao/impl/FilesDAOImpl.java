package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.FilesDAO;
import gov.healthit.chpl.dto.FileTypeDTO;
import gov.healthit.chpl.dto.FilesDTO;
import gov.healthit.chpl.entity.FileTypeEntity;
import gov.healthit.chpl.entity.FilesEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

@Repository(value = "filesDAO")
public class FilesDAOImpl extends BaseDAOImpl implements FilesDAO {

    @Override
    public FilesEntity create(final FilesDTO dto) throws EntityCreationException, EntityRetrievalException {
        FileTypeEntity type = new FileTypeEntity();
        type.setId(dto.getFileType().getId());

        FilesEntity insert = new FilesEntity();
        insert.setFileData(dto.getFileData());
        insert.setFileName(dto.getFileName());
        insert.setContentType(dto.getContentType());
        insert.setAssociatedDate(dto.getAssociatedDate());
        insert.setCreationDate(new Date());
        insert.setLastModifiedDate(new Date());
        if (dto.getDeleted() != null) {
            insert.setDeleted(dto.getDeleted());
        } else {
            insert.setDeleted(false);
        }
        if (dto.getLastModifiedUser() != null) {
            insert.setLastModifiedUser(dto.getLastModifiedUser());
        } else {
            insert.setLastModifiedUser(getUserId(SYSTEM_USER_ID));
        }
        insert.setFileType(type);

        entityManager.persist(insert);
        entityManager.flush();
        return insert;
    }

    @Override
    public void delete(final Long id) throws EntityRetrievalException {
        FilesEntity toDelete = getEntityById(id);
        if (toDelete != null) {
            toDelete.setDeleted(true);
            toDelete.setLastModifiedDate(new Date());
            if (Util.getCurrentUser() != null) {
                toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
            } else {
                toDelete.setLastModifiedUser(getUserId(SYSTEM_USER_ID));
            }
            entityManager.merge(toDelete);
        }

    }

    @Override
    public List<FilesDTO> findByFileType(final FileTypeDTO fileType) throws EntityRetrievalException {
        List<FilesDTO> dtos = new ArrayList<FilesDTO>();

        List<FilesEntity> entities = getEntityByFileTypeId(fileType.getId());

        for (FilesEntity entity : entities) {
            dtos.add(new FilesDTO(entity));
        }
        return dtos;
    }

    @Override
    public FilesDTO getById(final Long id) throws EntityRetrievalException {
        FilesDTO dto = null;
        FilesEntity file = this.getEntityById(id);

        if (file != null) {
            dto = new FilesDTO(file);
        }
        return dto;
    }

    public FilesEntity getEntityById(final Long id) throws EntityRetrievalException {
        FilesEntity entity = null;

        Query query = entityManager.createQuery(
                "FROM FilesEntity f "
                + "LEFT OUTER JOIN FETCH f.fileType "
                + "WHERE (NOT f.deleted = true) "
                + "AND (f.id = :entityid) ", FilesEntity.class);
        query.setParameter("entityid", id);
        List<FilesEntity> result = query.getResultList();

        if (result.size() > 1) {
            throw new EntityRetrievalException("Data error. Duplicate file id in database.");
        } else if (result.size() == 1) {
            entity = result.get(0);
        }

        return entity;
    }

    public List<FilesEntity> getEntityByFileTypeId(final Long fileTypeId) throws EntityRetrievalException {
        Query query = entityManager.createQuery(
                "FROM FilesEntity f "
                + "LEFT OUTER JOIN FETCH f.fileType ft "
                + "WHERE (NOT f.deleted = true) "
                + "AND (ft.id = :entityid) ", FilesEntity.class);
        query.setParameter("entityid", fileTypeId);
        List<FilesEntity> result = query.getResultList();

        return result;
    }
}
