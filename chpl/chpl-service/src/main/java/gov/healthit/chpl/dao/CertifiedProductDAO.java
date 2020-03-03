package gov.healthit.chpl.dao;

import java.util.Date;
import java.util.List;

import gov.healthit.chpl.domain.CertifiedProduct;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductSummaryDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.scheduler.job.urlStatus.UrlType;

/**
 * Interface for database access to CHPL listings.
 * @author kekey
 *
 */
public interface CertifiedProductDAO {

    CertifiedProductDTO create(CertifiedProductDTO product) throws EntityCreationException;

    CertifiedProductDTO update(CertifiedProductDTO product) throws EntityRetrievalException;

    void delete(Long productId);

    List<CertifiedProductDetailsDTO> findAll();

    List<CertifiedProductDetailsDTO> findByDeveloperId(Long developerId);

    List<CertifiedProductDetailsDTO> findByEdition(String edition);

    List<CertifiedProductDetailsDTO> findWithSurveillance();

    List<CertifiedProductDetailsDTO> findWithInheritance();

    CertifiedProductDTO getById(Long productId) throws EntityRetrievalException;

    CertifiedProductSummaryDTO getSummaryById(Long listingId) throws EntityRetrievalException;

    CertifiedProductDetailsDTO getDetailsById(Long productId) throws EntityRetrievalException;

    List<CertifiedProductDetailsDTO> getDetailsByIds(List<Long> productIds) throws EntityRetrievalException;

    CertifiedProductDTO getByChplNumber(String chplProductNumber);

    CertifiedProductDetailsDTO getByChplUniqueId(String chplUniqueId) throws EntityRetrievalException;

    Date getConfirmDate(Long listingId);

    List<CertifiedProductDetailsDTO> getDetailsByChplNumbers(List<String> chplProductNumbers);

    List<CertifiedProduct> getDetailsByVersionId(Long versionId);

    List<CertifiedProductDetailsDTO> getDetailsByProductId(Long productId);

    List<CertifiedProductDetailsDTO> getDetailsByAcbIds(List<Long> acbIds);

    List<CertifiedProduct> getDetailsByVersionAndAcbIds(Long versionId, List<Long> acbIds);

    List<CertifiedProductSummaryDTO> getSummaryByUrl(String url, UrlType urlType);

    List<CertifiedProductDTO> getByVersionIds(List<Long> versionIds);

    List<CertifiedProductDTO> getCertifiedProductsForDeveloper(Long vendorId);
}
