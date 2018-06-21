package gov.healthit.chpl.dao;

import java.util.List;
import java.util.Map;

import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.dto.CertificationIdAndCertifiedProductDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertificationIdDAO {

    CertificationIdDTO create(List<Long> productIds, String year) throws EntityCreationException;

    CertificationIdDTO create(CertificationIdDTO dto) throws EntityCreationException;

    List<CertificationIdDTO> findAll();

    CertificationIdDTO getById(Long id) throws EntityRetrievalException;

    CertificationIdDTO getByCertificationId(String certificationId) throws EntityRetrievalException;

    List<CertificationIdAndCertifiedProductDTO> getAllCertificationIdsWithProducts();

    CertificationIdDTO getByProductIds(List<Long> productIds, String year) throws EntityRetrievalException;

    Map<String, Boolean> verifyByCertificationId(List<String> certificationIds) throws EntityRetrievalException;

    List<Long> getProductIdsById(Long id) throws EntityRetrievalException;

    List<String> getCriteriaNumbersMetByCertifiedProductIds(List<Long> productIds);

    List<CQMMetDTO> getCqmsMetByCertifiedProductIds(List<Long> productIds);
}
