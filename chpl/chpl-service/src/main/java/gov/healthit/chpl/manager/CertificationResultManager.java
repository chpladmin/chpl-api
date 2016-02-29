package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;

public interface CertificationResultManager {
	
	public CertificationResultDTO create(Long acbId, CertificationResultDTO result) throws EntityRetrievalException, EntityCreationException;
	public CertificationResultDTO update(Long acbId, CertificationResultDTO result) throws EntityRetrievalException, EntityCreationException;

	
	public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareMappingsForCertificationResult(
			Long certificationResultId);
	public List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestStandardDTO> getTestStandardsForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestToolDTO> getTestToolsForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestDataDTO> getTestDataForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestProcedureDTO> getTestProceduresForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestFunctionalityDTO> getTestFunctionalityForCertificationResult(Long certificationResultId);
}
