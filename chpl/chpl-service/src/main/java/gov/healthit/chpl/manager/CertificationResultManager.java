package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareMapDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;

public interface CertificationResultManager {
	
	public void create(CertificationResultDTO result) throws EntityCreationException;
	public void update(CertificationResultDTO result) throws EntityRetrievalException, EntityCreationException;
	public void delete(Long resultId);
	public void deleteByCertifiedProductId(Long certifiedProductId);
	public List<CertificationResultDTO> getAll();
	public CertificationResultDTO getById(Long resultId) throws EntityRetrievalException;
	
	public CertificationResultAdditionalSoftwareMapDTO createAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareMapDTO dto) throws EntityCreationException;
	public CertificationResultAdditionalSoftwareMapDTO updateAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareMapDTO dto);
	public void deleteAdditionalSoftwareMapping(Long certificationResultId, Long additionalSoftwareId);
	public CertificationResultAdditionalSoftwareMapDTO getAdditionalSoftwareMapping(Long certificationResultId, Long additionalSoftwareId);
	public List<CertificationResultAdditionalSoftwareMapDTO> getAdditionalSoftwareMappingsForCertificationResult(
			Long certificationResultId);

	
}
