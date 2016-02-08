package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareMapDTO;

import java.util.List;

public interface CertificationResultDAO {
	
	public CertificationResultDTO create(CertificationResultDTO result) throws EntityCreationException;
	public CertificationResultDTO update(CertificationResultDTO result) throws EntityRetrievalException;
	public void delete(Long resultId);
	public void deleteByCertifiedProductId(Long certifiedProductId);
	public List<CertificationResultDTO> findAll();
	public List<CertificationResultDTO> findByCertifiedProductId(Long certifiedProductId);
	public CertificationResultDTO getById(Long resultId) throws EntityRetrievalException;
	
	public CertificationResultAdditionalSoftwareMapDTO createAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareMapDTO dto) throws EntityCreationException;
	public CertificationResultAdditionalSoftwareMapDTO updateAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareMapDTO dto);
	public void deleteAdditionalSoftwareMapping(Long certificationResultId, Long additionalSoftwareId);
	public CertificationResultAdditionalSoftwareMapDTO getAdditionalSoftwareMapping(Long certificationResultId, Long additionalSoftwareId);
	public List<CertificationResultAdditionalSoftwareMapDTO> getCertificationResultAdditionalSoftwareMappings(
			Long certificationResultId);
	
}
