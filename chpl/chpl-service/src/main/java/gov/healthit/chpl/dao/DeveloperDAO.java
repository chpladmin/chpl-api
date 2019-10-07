package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTO;
import gov.healthit.chpl.dto.DecertifiedDeveloperDTODeprecated;
import gov.healthit.chpl.dto.DeveloperACBMapDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.DeveloperStatusEventDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface DeveloperDAO {

    DeveloperDTO create(DeveloperDTO dto) throws EntityCreationException, EntityRetrievalException;

    DeveloperACBMapDTO createTransparencyMapping(DeveloperACBMapDTO dto);

    DeveloperDTO update(DeveloperDTO dto) throws EntityRetrievalException, EntityCreationException;
    void createDeveloperStatusEvent(DeveloperStatusEventDTO statusEventDto)
            throws EntityCreationException;
    void updateDeveloperStatusEvent(DeveloperStatusEventDTO statusEventDto)
            throws EntityRetrievalException;
    void deleteDeveloperStatusEvent(DeveloperStatusEventDTO statusEvent)
            throws EntityRetrievalException;

    void delete(Long id) throws EntityRetrievalException;

    List<DeveloperDTO> findAll();

    List<DeveloperDTO> findAllIncludingDeleted();

    DeveloperDTO getById(Long id) throws EntityRetrievalException;

    DeveloperDTO getById(Long id, boolean includeDeleted) throws EntityRetrievalException;

    DeveloperDTO getByName(String name);

    DeveloperDTO getByCode(String code);

    DeveloperDTO getByVersion(Long productVersionId) throws EntityRetrievalException;

    List<DeveloperDTO> getByWebsite(String website);

    DeveloperACBMapDTO updateTransparencyMapping(DeveloperACBMapDTO dto);

    void deleteTransparencyMapping(Long vendorId, Long acbId);

    DeveloperACBMapDTO getTransparencyMapping(Long vendorId, Long acbId);

    List<DeveloperTransparency> getAllDevelopersWithTransparencies();

    List<DeveloperACBMapDTO> getAllTransparencyMappings();

    @Deprecated
    List<DecertifiedDeveloperDTODeprecated> getDecertifiedDevelopers();
    List<DecertifiedDeveloperDTO> getDecertifiedDeveloperCollection();
}
