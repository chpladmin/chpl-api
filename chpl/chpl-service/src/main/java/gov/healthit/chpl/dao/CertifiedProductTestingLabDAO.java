package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductTestingLabDTO;

/**
 * Certified Product - Testing Lab mapping DAO.
 * @author alarned
 *
 */
public interface CertifiedProductTestingLabDAO {

    /**
     * Retrieve Testing Lab(s) for given Certified Product.
     * @param certifiedProductId the product
     * @return associated Testing labs
     * @throws EntityRetrievalException if entity cannot be retrieved
     */
    List<CertifiedProductTestingLabDTO> getTestingLabsByCertifiedProductId(Long certifiedProductId)
            throws EntityRetrievalException;

    /**
     * Look up mapping.
     * @param certifiedProductId the Listing ID
     * @param tlId the Testing Lab ID
     * @return the mapping between the two
     * @throws EntityRetrievalException if entity cannot be retrieved
     */
    CertifiedProductTestingLabDTO lookupMapping(Long certifiedProductId, Long tlId)
            throws EntityRetrievalException;

    /**
     * Create a mapping from a DTO.
     * @param toCreate the DTO
     * @return the mapping
     * @throws EntityCreationException if entity cannot be created
     */
    CertifiedProductTestingLabDTO createCertifiedProductTestingLab(CertifiedProductTestingLabDTO toCreate)
            throws EntityCreationException;

    /**
     * Delete a mapping.
     * @param id the id of the mapping
     * @return the deleted DTO
     * @throws EntityRetrievalException if entity cannot be retrieved
     */
    CertifiedProductTestingLabDTO deleteCertifiedProductTestingLab(Long id) throws EntityRetrievalException;

}
