package gov.healthit.chpl.manager;

import java.io.IOException;
import java.util.List;

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
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface CertificationResultManager {
    int update(Long abcId, CertifiedProductSearchDetails existingListing,
            CertifiedProductSearchDetails updatedListing, CertificationResult orig, CertificationResult updated)
            throws EntityCreationException, EntityRetrievalException, IOException;

    List<CertificationResultAdditionalSoftwareDTO> getAdditionalSoftwareMappingsForCertificationResult(
            Long certificationResultId);

    boolean getCertifiedProductHasAdditionalSoftware(Long certifiedProductId);

    List<CertificationResultUcdProcessDTO> getUcdProcessesForCertificationResult(Long certificationResultId);

    List<CertificationResultTestStandardDTO> getTestStandardsForCertificationResult(Long certificationResultId);

    List<CertificationResultTestToolDTO> getTestToolsForCertificationResult(Long certificationResultId);

    List<CertificationResultTestDataDTO> getTestDataForCertificationResult(Long certificationResultId);

    List<CertificationResultTestProcedureDTO> getTestProceduresForCertificationResult(
            Long certificationResultId);

    List<CertificationResultTestFunctionalityDTO> getTestFunctionalityForCertificationResult(
            Long certificationResultId);

    List<CertificationResultMacraMeasureDTO> getG1MacraMeasuresForCertificationResult(
            Long certificationResultId);

    List<CertificationResultMacraMeasureDTO> getG2MacraMeasuresForCertificationResult(
            Long certificationResultId);

    List<CertificationResultTestTaskDTO> getTestTasksForCertificationResult(Long certificationResultId);
}
