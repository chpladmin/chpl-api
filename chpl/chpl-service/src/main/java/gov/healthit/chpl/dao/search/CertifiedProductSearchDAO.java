package gov.healthit.chpl.dao.search;

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityNotFoundException;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.domain.IcsFamilyTreeNode;
import gov.healthit.chpl.domain.search.CertifiedProductBasicSearchResult;
import gov.healthit.chpl.domain.search.CertifiedProductFlatSearchResult;
import gov.healthit.chpl.domain.search.SearchRequest;

/**
 * Certified Product Search DAO.
 * @author alarned
 *
 */
public interface CertifiedProductSearchDAO {

    /**
     * Find database ID for Listing with passed in Product Number.
     * @param chplProductNumber entered product number
     * @return the database listing ID
     */
    Long getListingIdByUniqueChplNumber(String chplProductNumber);

    /**
     * Get the Certified Product by Product Number.
     * @param chplProductNumber entered product number
     * @return the Certified Product
     * @throws EntityNotFoundException if product not found
     */
    CertifiedProduct getByChplProductNumber(String chplProductNumber) throws EntityNotFoundException;

    /**
     * Get the flattened "all products" data.
     * @return all the products, flattened
     */
    List<CertifiedProductFlatSearchResult> getAllCertifiedProducts();

    /**
     * Retrieves the ICS family tree for a given certified product.
     * @param certifiedProductId database ID for a listing
     * @return the family tree
     */
    IcsFamilyTreeNode getICSFamilyTree(Long certifiedProductId);

    /**
     * Count the number of results for a search request.
     * @param searchRequest the search request
     * @return the number of results.
     */
    int getTotalResultCount(SearchRequest searchRequest);

    /**
     * Perform a basic search.
     * @param searchRequest the search request
     * @return a collection of basic results
     */
    Collection<CertifiedProductBasicSearchResult> search(SearchRequest searchRequest);
}
