package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;

public interface CertificationResultManager {
	
	public CertificationResultDTO create(Long acbId, CertificationResultDTO result) throws EntityCreationException;
	public CertificationResultDTO update(Long acbId, CertificationResultDTO result) throws EntityRetrievalException, EntityCreationException;
	public void delete(Long acbId, Long resultId);
	public void deleteByCertifiedProductId(Long acbId, Long certifiedProductId);
	public List<CertificationResultDTO> getAll();
	public CertificationResultDTO getById(Long resultId) throws EntityRetrievalException;
	
	public CertificationResultAdditionalSoftwareDTO createAdditionalSoftwareMapping(Long acbId, CertificationResultAdditionalSoftwareDTO dto) throws EntityCreationException;
	public CertificationResultAdditionalSoftwareDTO updateAdditionalSoftwareMapping(Long acbId, CertificationResultAdditionalSoftwareDTO dto);
	public void deleteAdditionalSoftwareMapping(Long acbId, Long id);
	public void deleteAdditionalSoftwareForCertificationResult(Long acbId, Long certificationResultId);
	public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareMappingsForCertificationResult(
			Long certificationResultId);

	
}
