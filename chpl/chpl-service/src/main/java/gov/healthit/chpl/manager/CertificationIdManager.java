package gov.healthit.chpl.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationIdDTO;

public interface CertificationIdManager {
	public CertificationIdDTO getByProductIds(List<Long> productIds, String year) throws EntityRetrievalException;
	public CertificationIdDTO getById(Long id) throws EntityRetrievalException;
	public CertificationIdDTO getByCertificationId(String certId) throws EntityRetrievalException;
	public Map<String, Boolean> verifyByCertificationId(List<String> certificationIds) throws EntityRetrievalException;
	public List<CertificationIdDTO> getAll();
	public CertificationIdDTO create(List<Long> productIds, String year) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
	public CertificationIdDTO create(CertificationIdDTO dto) throws EntityRetrievalException, EntityCreationException, JsonProcessingException;
}
