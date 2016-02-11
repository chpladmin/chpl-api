package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;

import java.util.List;

public interface CertificationResultDAO {
	
	public CertificationResultDTO create(CertificationResultDTO result) throws EntityCreationException;
	public CertificationResultDTO update(CertificationResultDTO result) throws EntityRetrievalException;
	public void delete(Long resultId);
	public void deleteByCertifiedProductId(Long certifiedProductId);
	public List<CertificationResultDTO> findAll();
	public List<CertificationResultDTO> findByCertifiedProductId(Long certifiedProductId);
	public CertificationResultDTO getById(Long resultId) throws EntityRetrievalException;
	
	public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareForCertificationResult(
			Long certificationResultId);
	public CertificationResultAdditionalSoftwareDTO addAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareDTO dto) throws EntityCreationException;
	public CertificationResultAdditionalSoftwareDTO updateAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareDTO dto);
	public void deleteAdditionalSoftwareMapping(Long mappingId);
}
