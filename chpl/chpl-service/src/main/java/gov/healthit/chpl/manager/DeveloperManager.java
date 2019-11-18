package gov.healthit.chpl.manager;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.DecertifiedDeveloper;
import gov.healthit.chpl.domain.DecertifiedDeveloperResult;
import gov.healthit.chpl.domain.DeveloperTransparency;
import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.MissingReasonException;
import gov.healthit.chpl.exception.ValidationException;

public interface DeveloperManager {
    String NEW_DEVELOPER_CODE = "XXXX";

    List<DeveloperDTO> getAll();

    List<DeveloperDTO> getAllIncludingDeleted();

    DeveloperDTO getById(Long id) throws EntityRetrievalException;
    DeveloperDTO getById(Long id, boolean allowDeleted)
            throws EntityRetrievalException;

    List<UserDTO> getAllUsersOnDeveloper(Long devId) throws EntityRetrievalException;

    List<DeveloperTransparency> getDeveloperCollection();

    DeveloperDTO update(DeveloperDTO developer, boolean doValidation)
            throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, MissingReasonException, ValidationException;

    DeveloperDTO create(DeveloperDTO dto)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    DeveloperDTO merge(List<Long> developerIdsToMerge, DeveloperDTO developerToCreate)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException, ValidationException;

    DeveloperDTO split(DeveloperDTO oldDeveloper, DeveloperDTO developerToCreate, List<Long> productIdsToMove)
            throws AccessDeniedException, EntityRetrievalException, EntityCreationException,
            JsonProcessingException, ValidationException;

    void validateDeveloperInSystemIfExists(PendingCertifiedProductDetails pendingCp)
            throws EntityRetrievalException, ValidationException;

    @Deprecated
    List<DecertifiedDeveloperResult> getDecertifiedDevelopers() throws EntityRetrievalException;
    List<DecertifiedDeveloper> getDecertifiedDeveloperCollection();
}
