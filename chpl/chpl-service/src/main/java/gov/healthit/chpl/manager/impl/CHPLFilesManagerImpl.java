package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.CHPLFileDAO;
import gov.healthit.chpl.dto.CHPLFileDTO;
import gov.healthit.chpl.dto.FileTypeDTO;
import gov.healthit.chpl.entity.CHPLFileEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.CHPLFileManager;

@Service
public class CHPLFilesManagerImpl implements CHPLFileManager {
    private static Long API_DOCUMENTATION_FILE_TYPE = 1L;
    private CHPLFileDAO chplFileDAO;

    @Autowired
    public CHPLFilesManagerImpl(final CHPLFileDAO chplFileDAO) {
        this.chplFileDAO = chplFileDAO;
    }

    @Override
    @Transactional
    //@PreAuthorize("hasRole('ROLE_ADMIN')")
    public CHPLFileDTO addApiDocumentationFile(final CHPLFileDTO newFileDTO)
            throws EntityCreationException, EntityRetrievalException {
        //Need to delete the existing 'current' file
        CHPLFileDTO toDelete = getApiDocumentation();
        if (toDelete != null) {
            chplFileDAO.delete(toDelete.getId());
        }

        //Add the new Api Documentation
        FileTypeDTO fileTypeDTO = new FileTypeDTO();
        fileTypeDTO.setId(API_DOCUMENTATION_FILE_TYPE);
        newFileDTO.setFileType(fileTypeDTO);

        CHPLFileEntity entity = chplFileDAO.create(newFileDTO);

        CHPLFileDTO created = new CHPLFileDTO(entity);

        return created;
    }

    @Override
    public CHPLFileDTO getApiDocumentation() throws EntityRetrievalException {
        FileTypeDTO fileTypeDTO = new FileTypeDTO();
        fileTypeDTO.setId(API_DOCUMENTATION_FILE_TYPE);

        List<CHPLFileDTO> dtos = chplFileDAO.findByFileType(fileTypeDTO);

        //Business logic dictates there should only be one active Api Doc file
        if (dtos != null && dtos.size() > 0) {
            return dtos.get(0);
        } else {
            return null;
        }
    }
}
