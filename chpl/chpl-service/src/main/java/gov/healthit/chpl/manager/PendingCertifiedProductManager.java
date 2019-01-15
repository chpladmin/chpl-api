package gov.healthit.chpl.manager;

import java.util.List;

import javax.persistence.EntityNotFoundException;

import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ObjectMissingValidationException;
import gov.healthit.chpl.exception.ValidationException;

public interface PendingCertifiedProductManager {
    PendingCertifiedProductDetails getById(Long id)
            throws EntityNotFoundException, EntityRetrievalException, AccessDeniedException, ValidationException;
    PendingCertifiedProductDetails getById(Long id, boolean includeDeleted)
            throws EntityRetrievalException, AccessDeniedException, ValidationException;
    public PendingCertifiedProductDetails getByIdForActivity(final Long id)
            throws EntityRetrievalException, AccessDeniedException, ValidationException;

    List<PendingCertifiedProductDTO> getAllPendingCertifiedProducts() throws ValidationException;
    List<PendingCertifiedProductDTO> getPendingCertifiedProducts(Long acbId) throws ValidationException;

    PendingCertifiedProductDTO createOrReplace(Long acbId, PendingCertifiedProductEntity toCreate)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    void deletePendingCertifiedProduct(final Long acbId, Long pendingProductId)
            throws EntityRetrievalException, EntityNotFoundException, EntityCreationException, AccessDeniedException,
            JsonProcessingException, ObjectMissingValidationException;

    void confirm(Long acbId, Long pendingProductId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    boolean isPendingListingAvailableForUpdate(Long acbId, PendingCertifiedProductDTO pendingCp)
            throws EntityRetrievalException, ObjectMissingValidationException;

    boolean isPendingListingAvailableForUpdate(Long acbId, Long pendingProductId)
            throws EntityRetrievalException, ObjectMissingValidationException;

    void addAllVersionsToCmsCriterion(PendingCertifiedProductDetails pcpDetails);

    void addAllMeasuresToCertificationCriteria(PendingCertifiedProductDetails pcpDetails);

    void addAvailableTestFunctionalities(PendingCertifiedProductDetails pcpDetails);
}
