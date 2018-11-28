package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.FileTypeDTO;
import gov.healthit.chpl.dto.FilesDTO;
import gov.healthit.chpl.entity.FilesEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface FilesDAO {
    FilesEntity create(FilesDTO dto) throws EntityCreationException, EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<FilesDTO> findByFileType(FileTypeDTO dto) throws EntityRetrievalException;

    FilesDTO getById(Long id) throws EntityRetrievalException;
}
