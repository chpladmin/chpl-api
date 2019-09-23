package gov.healthit.chpl.manager;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;

public interface UserPermissionsManager {
    void addAcbPermission(CertificationBodyDTO acb, Long userId)
            throws EntityRetrievalException, UserRetrievalException;

    void deleteAcbPermission(CertificationBodyDTO acb, Long userId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    void addAtlPermission(TestingLabDTO acb, Long userId) throws EntityRetrievalException, UserRetrievalException;

    void deleteAtlPermission(TestingLabDTO acb, Long userId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    void addDeveloperPermission(DeveloperDTO developer, Long userId)
            throws EntityRetrievalException, UserRetrievalException;

    void deleteDeveloperPermission(Long developerId, Long userId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

}
