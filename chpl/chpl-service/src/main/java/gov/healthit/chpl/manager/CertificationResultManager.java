package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.dto.CertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.CertificationResultMacraMeasureDTO;
import gov.healthit.chpl.dto.CertificationResultTestDataDTO;
import gov.healthit.chpl.dto.CertificationResultTestFunctionalityDTO;
import gov.healthit.chpl.dto.CertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.CertificationResultTestStandardDTO;
import gov.healthit.chpl.dto.CertificationResultTestTaskDTO;
import gov.healthit.chpl.dto.CertificationResultTestToolDTO;
import gov.healthit.chpl.dto.CertificationResultUcdProcessDTO;

public interface CertificationResultManager {
	public int update(Long abcId, CertifiedProductSearchDetails existingListing, 
			CertifiedProductSearchDetails updatedListing, CertificationResult orig, CertificationResult updated)
			throws EntityCreationException, EntityRetrievalException;
	
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
}
