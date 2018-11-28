package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.FilesDAO;
import gov.healthit.chpl.dto.FileTypeDTO;
import gov.healthit.chpl.dto.FilesDTO;
import gov.healthit.chpl.entity.FilesEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.FilesManager;

@Service
public class FilesManagerImpl implements FilesManager {
    private static Long API_DOCUMENTATION_FILE_TYPE = 1l;
    private FilesDAO filesDAO;

    @Autowired
    public FilesManagerImpl(final FilesDAO filesDAO) {
        this.filesDAO = filesDAO;
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public FilesDTO addApiDocumentationFile(final FilesDTO newFileDTO) 
            throws EntityCreationException, EntityRetrievalException {
        //Need to delete the existing 'current' file
        FilesDTO toDelete = getApiDocumentation();
        if (toDelete != null) {
            filesDAO.delete(toDelete.getId());
        }

        //Add the new Api Documentation
        FileTypeDTO fileTypeDTO = new FileTypeDTO();
        fileTypeDTO.setId(API_DOCUMENTATION_FILE_TYPE);
        newFileDTO.setFileType(fileTypeDTO);

        FilesEntity entity = filesDAO.create(newFileDTO);

        FilesDTO created = new FilesDTO(entity);

        return created;
    }

    @Override
    public FilesDTO getApiDocumentation() throws EntityRetrievalException {
        FileTypeDTO fileTypeDTO = new FileTypeDTO();
        fileTypeDTO.setId(API_DOCUMENTATION_FILE_TYPE);

        List<FilesDTO> dtos = filesDAO.findByFileType(fileTypeDTO);

        //Business logic dictates there should only be one active Api Doc file
        if (dtos != null && dtos.size() > 0) {
            return dtos.get(0);
        } else {
            return null;
        }
    }

}
