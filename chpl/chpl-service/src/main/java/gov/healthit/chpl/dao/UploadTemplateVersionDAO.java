package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.UploadTemplateVersionDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface UploadTemplateVersionDAO {
    List<UploadTemplateVersionDTO> findAll();
    UploadTemplateVersionDTO getById(Long id) throws EntityRetrievalException;
}
