package gov.healthit.chpl.manager;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.SimpleCertificationId;
import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertificationIdManager {
    CertificationIdDTO getByProductIds(List<Long> productIds, String year) throws EntityRetrievalException;

    CertificationIdDTO getById(Long id) throws EntityRetrievalException;

    CertificationIdDTO getByCertificationId(String certId) throws EntityRetrievalException;

    List<Long> getProductIdsById(Long id) throws EntityRetrievalException;

    Map<String, Boolean> verifyByCertificationId(List<String> certificationIds) throws EntityRetrievalException;

    List<SimpleCertificationId> getAll();

    List<SimpleCertificationId> getAllWithProducts();

    CertificationIdDTO create(List<Long> productIds, String year)
            throws EntityRetrievalException, EntityCreationException, JsonProcessingException;

    List<String> getCriteriaNumbersMetByCertifiedProductIds(List<Long> productIds);

    List<CQMMetDTO> getCqmsMetByCertifiedProductIds(List<Long> productIds);
}
