package gov.healthit.chpl.manager;

import gov.healthit.chpl.dto.FilesDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface FilesManager {
    FilesDTO addApiDocumentationFile(FilesDTO newFileDTO) throws EntityCreationException, EntityRetrievalException;

    FilesDTO getApiDocumentation() throws EntityRetrievalException;
}
