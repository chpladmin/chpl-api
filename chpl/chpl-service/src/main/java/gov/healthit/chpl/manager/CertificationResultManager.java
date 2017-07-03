package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskParticipantDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.CertifiedProductDTO;

public interface CertificationResultManager {
	
	public CertificationResultDTO update(Long acbId, CertifiedProductDTO listing, CertificationResultDTO result) throws EntityRetrievalException, EntityCreationException;

	
	public List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareMappingsForCertificationResult(
			Long certificationResultId);
	public List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestStandardDTO> getTestStandardsForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestToolDTO> getTestToolsForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestDataDTO> getTestDataForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestProcedureDTO> getTestProceduresForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestFunctionalityDTO> getTestFunctionalityForCertificationResult(Long certificationResultId);
	public List<CertificationResultMacraMeasureDTO> getG1MacraMeasuresForCertificationResult(Long certificationResultId);
	public List<CertificationResultMacraMeasureDTO> getG2MacraMeasuresForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestTaskDTO> getTestTasksForCertificationResult(Long certificationResultId);
	public List<CertificationResultTestTaskParticipantDTO> getTestParticipantsForTask(Long certificationResultTaskId);
}
