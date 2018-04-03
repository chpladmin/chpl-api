package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;

/**
 * Data access object for the certified_product_details view.
 * @author TYoung
 *
 */
public interface CertifiedProductSearchResultDAO {

    /**
     * Retrieves the matching certified_product_details record as a CertifiedProductDetailsDTO
     * objects.
     * @param productId Id of the product to retrieve
     * @throws EntityRetrievalException if unable to retrieve the entity
     * @return CertifiedProductDetailsDTO
     */
    CertifiedProductDetailsDTO getById(Long productId) throws EntityRetrievalException;

    /**
     * Retrieves a list of certified_product_details records as a list of CertifiedProductDetailsDTOs
     * that match the chplProductNumber parameter.
     * There should only be one item returned.
     * @param chplProductNumber String
     * @throws EntityRetrievalException if unable to retrieve the entity
     * @return List<CertifiedProductDetailsDTO>
     */
    List<CertifiedProductDetailsDTO> getByChplProductNumber(String chplProductNumber) throws EntityRetrievalException;

}
