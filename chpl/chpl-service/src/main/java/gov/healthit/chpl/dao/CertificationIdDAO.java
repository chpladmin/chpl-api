package gov.healthit.chpl.dao;

import java.util.List;
import java.util.Map;

import gov.healthit.chpl.dto.CQMMetDTO;
import gov.healthit.chpl.dto.CertificationIdAndCertifiedProductDTO;
import gov.healthit.chpl.dto.CertificationIdDTO;

public interface CertificationIdDAO {

	public CertificationIdDTO create(List<Long> productIds, String year) throws EntityCreationException;
	public CertificationIdDTO create(CertificationIdDTO dto) throws EntityCreationException;

	public List<CertificationIdDTO> findAll();

	public CertificationIdDTO getById(Long id) throws EntityRetrievalException;
	public CertificationIdDTO getByCertificationId(String certificationId) throws EntityRetrievalException;
	public List<CertificationIdAndCertifiedProductDTO> getAllCertificationIdsWithProducts();
	public CertificationIdDTO getByProductIds(List<Long> productIds, String year) throws EntityRetrievalException;
	public Map<String, Boolean> verifyByCertificationId(List<String> certificationIds) throws EntityRetrievalException;
	public List<Long> getProductIdsById(Long id) throws EntityRetrievalException;
	public List<String> getCriteriaNumbersMetByCertifiedProductIds(List<Long> productIds);
	public List<CQMMetDTO> getCqmsMetByCertifiedProductIds(List<Long> productIds);
}
