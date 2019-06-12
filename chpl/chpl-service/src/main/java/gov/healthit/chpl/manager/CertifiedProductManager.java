package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;

public interface CertifiedProductManager {

    CertifiedProductDTO getById(Long id) throws EntityRetrievalException;

    CertifiedProductDTO getByChplProductNumber(String chplProductNumber) throws EntityRetrievalException;

    boolean chplIdExists(String id) throws EntityRetrievalException;

    List<CertifiedProductDetailsDTO> getDetailsByIds(List<Long> ids) throws EntityRetrievalException;

    List<CertifiedProductDetailsDTO> getAll();

    List<CertifiedProductDetailsDTO> getByProduct(Long productId) throws EntityRetrievalException;

    List<CertifiedProductDetailsDTO> getByVersion(Long versionId) throws EntityRetrievalException;

    List<CertifiedProductDetailsDTO> getByVersionWithEditPermission(Long versionId)
            throws EntityRetrievalException;
    List<CertifiedProductDetailsDTO> getByAcbWithOpenSurveillance(Long acbId, Date survDate);

    CertifiedProductDTO changeOwnership(Long certifiedProductId, Long acbId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    CertifiedProductDTO update(Long acbId, ListingUpdateRequest updateRequest,
            CertifiedProductSearchDetails existingListing) throws EntityRetrievalException, JsonProcessingException,
            EntityCreationException, InvalidArgumentsException, IOException, ValidationException;

    void sanitizeUpdatedListingData(Long acbId, CertifiedProductSearchDetails listing)
            throws EntityNotFoundException;

    CertifiedProductDTO createFromPending(Long acbId, PendingCertifiedProductDTO pendingCp)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException, IOException;

    List<IcsFamilyTreeNode> getIcsFamilyTree(Long certifiedProductId) throws EntityRetrievalException;

    List<IcsFamilyTreeNode> getIcsFamilyTree(String chplProductNumber) throws EntityRetrievalException;

    CertifiedProductDetailsDTO getDetailsById(Long ids) throws EntityRetrievalException;

    List<CertifiedProductDetailsDTO> getByDeveloperId(Long developerId) throws EntityRetrievalException;
}
