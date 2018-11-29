package gov.healthit.chpl.manager;

import gov.healthit.chpl.dto.CHPLFileDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CHPLFileManager {
    CHPLFileDTO addApiDocumentationFile(CHPLFileDTO newFileDTO) throws EntityCreationException, EntityRetrievalException;

    CHPLFileDTO getApiDocumentation() throws EntityRetrievalException;
}
