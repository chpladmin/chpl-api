package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.FileTypeDTO;
import gov.healthit.chpl.dto.CHPLFileDTO;
import gov.healthit.chpl.entity.CHPLFileEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CHPLFileDAO {
    CHPLFileEntity create(CHPLFileDTO dto) throws EntityCreationException, EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<CHPLFileDTO> findByFileType(FileTypeDTO dto) throws EntityRetrievalException;

    CHPLFileDTO getById(Long id) throws EntityRetrievalException;
}
