package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

/**
 * Data Access Object interface for pending Certified Products.
 * @author alarned
 *
 */
public interface PendingCertifiedProductDAO {

    /**
     * Create a pending Certified Product.
     * @param product the entity to create from
     * @return a data transfer object
     * @throws EntityCreationException if creation doesn't work
     */
    PendingCertifiedProductDTO create(PendingCertifiedProductEntity product) throws EntityCreationException;

    /**
     * Delete a pending Certified Product.
     * @param pendingProductId the product's id
     * @throws EntityRetrievalException if entity retrieval fails
     */
    void delete(Long pendingProductId) throws EntityRetrievalException;

    /**
     * Return all of the pending Certified Products.
     * @return DTOs of the Products
     */
    List<PendingCertifiedProductDTO> findAll();

    /**
     * Return all pending Certified Products with a given status.
     * @param statusId the status id
     * @return DTOs of the Products
     */
    List<PendingCertifiedProductDTO> findByStatus(Long statusId);

    /**
     * Retrieve a pending Certified Product by CHPL ID.
     * @param id the CHPL ID
     * @return the pending Certified Product
     * @throws EntityRetrievalException if the entity cannot be retrieved
     */
    Long findIdByOncId(String id) throws EntityRetrievalException;

    /**
     * Return the pending Certified Product by database id.
     * @param pcpId the database id
     * @param includeDeleted true if deleted Products are requested
     * @return the relevant Product
     * @throws EntityRetrievalException if the entity cannot be retrieved
     */
    PendingCertifiedProductDTO findById(Long pcpId, boolean includeDeleted) throws EntityRetrievalException;

    /**
     * Returns the ACB ID associated with a given pending listing. Useful for authorization.
     * @param pcpId
     * @return
     * @throws EntityRetrievalException
     */
    Long findAcbIdById(final Long pcpId) throws EntityRetrievalException;

    /**
     * Return all pending Certified Products at a given ACB.
     * @param acbId the ACB's ID
     * @return DTOs of the relevant Products
     */
    List<PendingCertifiedProductDTO> findByAcbId(Long acbId);
}
