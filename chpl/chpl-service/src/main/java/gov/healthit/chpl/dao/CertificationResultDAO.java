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
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertificationResultDAO {

    List<Long> getCpIdsByCriterionId(Long criterionId) throws EntityRetrievalException;

    CertificationResultDTO create(CertificationResultDTO result) throws EntityCreationException;

    CertificationResultDTO update(CertificationResultDTO result) throws EntityRetrievalException;

    void delete(Long resultId);

    void deleteByCertifiedProductId(Long certifiedProductId);

    List<CertificationResultDTO> findByCertifiedProductId(Long certifiedProductId);

    CertificationResultDTO getById(Long resultId) throws EntityRetrievalException;

    List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareForCertificationResult(
            Long certificationResultId);

    CertificationResultAdditionalSoftwareDTO addAdditionalSoftwareMapping(CertificationResultAdditionalSoftwareDTO dto)
            throws EntityCreationException;

    void deleteAdditionalSoftwareMapping(Long mappingId);

    CertificationResultAdditionalSoftwareDTO updateAdditionalSoftwareMapping(
            CertificationResultAdditionalSoftwareDTO toUpdate) throws EntityRetrievalException;

    boolean getCertifiedProductHasAdditionalSoftware(Long certifiedProductId);

    List<CertificationResultTestStandardDTO> getTestStandardsForCertificationResult(Long certificationResultId);

    CertificationResultTestStandardDTO addTestStandardMapping(CertificationResultTestStandardDTO dto)
            throws EntityCreationException;

    CertificationResultTestStandardDTO lookupTestStandardMapping(Long certificationResultId, Long testStandardId);

    void deleteTestStandardMapping(Long mappingId);

    List<CertificationResultTestToolDTO> getTestToolsForCertificationResult(Long certificationResultId);

    CertificationResultTestToolDTO addTestToolMapping(CertificationResultTestToolDTO dto)
            throws EntityCreationException;

    void deleteTestToolMapping(Long mappingId);

    List<CertificationResultMacraMeasureDTO> getG1MacraMeasuresForCertificationResult(Long certificationResultId);

    List<CertificationResultMacraMeasureDTO> getG2MacraMeasuresForCertificationResult(Long certificationResultId);

    CertificationResultMacraMeasureDTO addG1MacraMeasureMapping(CertificationResultMacraMeasureDTO dto)
            throws EntityCreationException;

    CertificationResultMacraMeasureDTO addG2MacraMeasureMapping(CertificationResultMacraMeasureDTO dto)
            throws EntityCreationException;

    void deleteG1MacraMeasureMapping(Long certificationResultId, Long macraMeasureId);

    void deleteG2MacraMeasureMapping(Long certificationResultId, Long macraMeasureId);

    List<CertificationResultTestDataDTO> getTestDataForCertificationResult(Long certificationResultId);

    CertificationResultTestDataDTO addTestDataMapping(CertificationResultTestDataDTO dto)
            throws EntityCreationException;

    void deleteTestDataMapping(Long mappingId);

    void updateTestDataMapping(CertificationResultTestDataDTO dto) throws EntityRetrievalException;

    List<CertificationResultTestProcedureDTO> getTestProceduresForCertificationResult(Long certificationResultId);

    CertificationResultTestProcedureDTO addTestProcedureMapping(CertificationResultTestProcedureDTO dto)
            throws EntityCreationException;

    void deleteTestProcedureMapping(Long mappingId);

    List<CertificationResultTestFunctionalityDTO> getTestFunctionalityForCertificationResult(
            Long certificationResultId);

    CertificationResultTestFunctionalityDTO addTestFunctionalityMapping(CertificationResultTestFunctionalityDTO dto)
            throws EntityCreationException;

    void deleteTestFunctionalityMapping(Long mappingId);

    List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId);

    CertificationResultUcdProcessDTO lookupUcdProcessMapping(Long certificationResultId, Long ucdProcessId);

    CertificationResultUcdProcessDTO addUcdProcessMapping(CertificationResultUcdProcessDTO dto)
            throws EntityCreationException;

    void deleteUcdProcessMapping(Long certResultId, Long ucdProcessId);

    void updateUcdProcessMapping(CertificationResultUcdProcessDTO dto) throws EntityRetrievalException;

    List<CertificationResultTestTaskDTO> getTestTasksForCertificationResult(Long certificationResultId);

    CertificationResultTestTaskDTO addTestTaskMapping(CertificationResultTestTaskDTO dto)
            throws EntityCreationException;

    void deleteTestTaskMapping(Long certResultId, Long testTaskId);

    TestParticipantDTO addTestParticipantMapping(TestTaskDTO task, TestParticipantDTO dto)
            throws EntityCreationException;

    void deleteTestParticipantMapping(Long testTaskId, Long testParticipantId);
}
