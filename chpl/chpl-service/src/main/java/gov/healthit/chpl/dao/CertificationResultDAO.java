package gov.healthit.chpl.dao;

import gov.healthit.chpl.dto.CertificationResultDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;
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
	
	public List<CertificationResultTestStandardDTO> getTestStandardsForCertificationResult(Long certificationResultId);
	public CertificationResultTestStandardDTO addTestStandardMapping(CertificationResultTestStandardDTO dto) throws EntityCreationException;
	public void deleteTestStandardMapping(Long mappingId);
	
	public List<CertificationResultTestToolDTO> getTestToolsForCertificationResult(Long certificationResultId);
	public CertificationResultTestToolDTO addTestToolMapping(CertificationResultTestToolDTO dto) throws EntityCreationException;
	public void deleteTestToolMapping(Long mappingId);
	
	public List<CertificationResultTestDataDTO> getTestDataForCertificationResult(Long certificationResultId);
	public CertificationResultTestDataDTO updateTestDataMapping(CertificationResultTestDataDTO dto);
	public CertificationResultTestDataDTO addTestDataMapping(CertificationResultTestDataDTO dto) throws EntityCreationException;
	public void deleteTestDataMapping(Long mappingId);
	
	public List<CertificationResultTestProcedureDTO> getTestProceduresForCertificationResult(Long certificationResultId);
	public CertificationResultTestProcedureDTO addTestProcedureMapping(CertificationResultTestProcedureDTO dto) throws EntityCreationException;
	public void deleteTestProcedureMapping(Long mappingId);
	
	public List<CertificationResultTestFunctionalityDTO> getTestFunctionalityForCertificationResult(Long certificationResultId);
	public CertificationResultTestFunctionalityDTO addTestFunctionalityMapping(CertificationResultTestFunctionalityDTO dto) throws EntityCreationException;
	public void deleteTestFunctionalityMapping(Long mappingId);
	
	public List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId);
	public CertificationResultUcdProcessDTO addUcdProcessMapping(CertificationResultUcdProcessDTO dto) throws EntityCreationException;
	public void deleteUcdProcessMapping(Long mappingId);
}

