package gov.healthit.chpl.dao;

import java.util.List;

import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
import gov.healthit.chpl.dto.TestParticipantDTO;
import gov.healthit.chpl.dto.TestTaskDTO;

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
	public void deleteAdditionalSoftwareMapping(Long mappingId);
	public CertificationResultAdditionalSoftwareDTO updateAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareDTO toUpdate) throws EntityRetrievalException;
	
	public List<CertificationResultTestStandardDTO> getTestStandardsForCertificationResult(Long certificationResultId);
	public CertificationResultTestStandardDTO addTestStandardMapping(CertificationResultTestStandardDTO dto) throws EntityCreationException;
	public CertificationResultTestStandardDTO lookupTestStandardMapping(Long certificationResultId, Long testStandardId);
	public void deleteTestStandardMapping(Long mappingId);
	
	public List<CertificationResultTestToolDTO> getTestToolsForCertificationResult(Long certificationResultId);
	public CertificationResultTestToolDTO addTestToolMapping(CertificationResultTestToolDTO dto) throws EntityCreationException;
	public void deleteTestToolMapping(Long mappingId);
	
	public List<CertificationResultMacraMeasureDTO> getG1MacraMeasuresForCertificationResult(Long certificationResultId);
	public List<CertificationResultMacraMeasureDTO> getG2MacraMeasuresForCertificationResult(Long certificationResultId);
	public CertificationResultMacraMeasureDTO addG1MacraMeasureMapping(CertificationResultMacraMeasureDTO dto) throws EntityCreationException;
	public CertificationResultMacraMeasureDTO addG2MacraMeasureMapping(CertificationResultMacraMeasureDTO dto) throws EntityCreationException;
	public void deleteG1MacraMeasureMapping(Long certificationResultId, Long macraMeasureId);
	public void deleteG2MacraMeasureMapping(Long certificationResultId, Long macraMeasureId);
	
	public List<CertificationResultTestDataDTO> getTestDataForCertificationResult(Long certificationResultId);
	public CertificationResultTestDataDTO addTestDataMapping(CertificationResultTestDataDTO dto) throws EntityCreationException;
	public void deleteTestDataMapping(Long mappingId);
	public void updateTestDataMapping(CertificationResultTestDataDTO dto) throws EntityRetrievalException;

	public List<CertificationResultTestProcedureDTO> getTestProceduresForCertificationResult(Long certificationResultId);
	public CertificationResultTestProcedureDTO addTestProcedureMapping(CertificationResultTestProcedureDTO dto) throws EntityCreationException;
	public void deleteTestProcedureMapping(Long mappingId);
	
	public List<CertificationResultTestFunctionalityDTO> getTestFunctionalityForCertificationResult(Long certificationResultId);
	public CertificationResultTestFunctionalityDTO addTestFunctionalityMapping(CertificationResultTestFunctionalityDTO dto) throws EntityCreationException;
	public void deleteTestFunctionalityMapping(Long mappingId);
	
	public List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId);
	public CertificationResultUcdProcessDTO lookupUcdProcessMapping(Long certificationResultId, Long ucdProcessId);
	public CertificationResultUcdProcessDTO addUcdProcessMapping(CertificationResultUcdProcessDTO dto) throws EntityCreationException;
	public void deleteUcdProcessMapping(Long mappingId);
	public void updateUcdProcessMapping(CertificationResultUcdProcessDTO dto) throws EntityRetrievalException;
	
	public List<CertificationResultTestTaskDTO> getTestTasksForCertificationResult(Long certificationResultId);
	public CertificationResultTestTaskDTO addTestTaskMapping(CertificationResultTestTaskDTO dto) throws EntityCreationException;
	public void deleteTestTaskMapping(Long mappingId);
	
	public TestParticipantDTO addTestParticipantMapping(TestTaskDTO task, TestParticipantDTO dto) throws EntityCreationException;
	public void deleteTestParticipantMapping(Long testTaskId, Long testParticipantId);
}

