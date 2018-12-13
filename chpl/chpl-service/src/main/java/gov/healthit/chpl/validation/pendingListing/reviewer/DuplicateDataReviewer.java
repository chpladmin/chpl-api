package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestDataDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestProcedureDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("pendingDuplicateDataReviewer")
public class DuplicateDataReviewer implements Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(DuplicateDataReviewer.class);

    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public DuplicateDataReviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO pcr : listing.getCertificationCriterion()) {
            //Additional Software
            DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO> addtlSoftwareDuplicateResults =
                    removeDuplicateAdditionalSoftware(pcr);
            if (addtlSoftwareDuplicateResults.getMessages().size() > 0) {
                listing.getWarningMessages().addAll(addtlSoftwareDuplicateResults.getMessages());
                pcr.setAdditionalSoftware(addtlSoftwareDuplicateResults.getObjects());
            }

            //Test Tool
            DuplicateValidationResult<PendingCertificationResultTestToolDTO> testToolDuplicateResults =
                    removeDuplicateTestTool(pcr);
            if (testToolDuplicateResults.getMessages().size() > 0) {
                listing.getWarningMessages().addAll(testToolDuplicateResults.getMessages());
                pcr.setTestTools(testToolDuplicateResults.getObjects());
            }

            //Test Procedure
            DuplicateValidationResult<PendingCertificationResultTestProcedureDTO> testProcedureDuplicateResults =
                    removeDuplicateTestProcedure(pcr);
            if (testProcedureDuplicateResults.getMessages().size() > 0) {
                listing.getWarningMessages().addAll(testProcedureDuplicateResults.getMessages());
                pcr.setTestProcedures(testProcedureDuplicateResults.getObjects());
            }

            //Test Data
            DuplicateValidationResult<PendingCertificationResultTestDataDTO> testDataDuplicateResults =
                    removeDuplicateTestData(pcr);
            if (testDataDuplicateResults.getMessages().size() > 0) {
                listing.getWarningMessages().addAll(testDataDuplicateResults.getMessages());
                pcr.setTestData(testDataDuplicateResults.getObjects());
            }
        }
    }

    private DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO> removeDuplicateAdditionalSoftware(
            PendingCertificationResultDTO certificationResult) {

        DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO> dupResults =
                new DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO>();

        if (certificationResult.getAdditionalSoftware() != null) {
            for (PendingCertificationResultAdditionalSoftwareDTO dto : certificationResult.getAdditionalSoftware()) {
                if (isAdditionalSoftwareDuplicate(dupResults, dto)) {
                    // Item already exists
                    String warning = errorMessageUtil.getMessage("listing.criteria.duplicateAdditionalSoftware",
                            certificationResult.getNumber(), dto.getName(), dto.getVersion());
                    dupResults.getMessages().add(warning);
                } else {
                    //Add the item to the final list
                    dupResults.getObjects().add(dto);
                }
            }
        }

        return dupResults;
    }

    private Boolean isAdditionalSoftwareDuplicate(
            DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO> dupResults,
            PendingCertificationResultAdditionalSoftwareDTO additionalSoftwareDTO) {
        return dupResults.existsInObjects(additionalSoftwareDTO,
                new Predicate<PendingCertificationResultAdditionalSoftwareDTO>() {
            @Override
            public boolean test(PendingCertificationResultAdditionalSoftwareDTO dto2) {
                return additionalSoftwareDTO.getName().equals(dto2.getName())
                        && additionalSoftwareDTO.getVersion().equals(dto2.getVersion());
            }
        });
    }

    private DuplicateValidationResult<PendingCertificationResultTestToolDTO> removeDuplicateTestTool(
            PendingCertificationResultDTO certificationResult) {

        DuplicateValidationResult<PendingCertificationResultTestToolDTO> dupResults =
                new DuplicateValidationResult<PendingCertificationResultTestToolDTO>();

        if (certificationResult.getTestTools() != null) {
            for (PendingCertificationResultTestToolDTO dto : certificationResult.getTestTools()) {
                if (isTestToolDuplicate(dupResults, dto)) {
                    // Item already exists
                    String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestTool",
                            certificationResult.getNumber(), dto.getName(), dto.getVersion());
                    dupResults.getMessages().add(warning);
                } else {
                    //Add the item to the final list
                    dupResults.getObjects().add(dto);
                }
            }
        }

        return dupResults;
    }

    private Boolean isTestToolDuplicate(DuplicateValidationResult<PendingCertificationResultTestToolDTO> dupResults,
            PendingCertificationResultTestToolDTO testToolDTO) {
        return dupResults.existsInObjects(testToolDTO, new Predicate<PendingCertificationResultTestToolDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestToolDTO dto2) {
                return testToolDTO.getName().equals(dto2.getName()) && testToolDTO.getVersion().equals(dto2.getVersion());
            }
        });
    }

    private DuplicateValidationResult<PendingCertificationResultTestProcedureDTO> removeDuplicateTestProcedure(
            PendingCertificationResultDTO certificationResult) {

        DuplicateValidationResult<PendingCertificationResultTestProcedureDTO> dupResults =
                new DuplicateValidationResult<PendingCertificationResultTestProcedureDTO>();

        if (certificationResult.getTestProcedures() != null) {
            for (PendingCertificationResultTestProcedureDTO dto : certificationResult.getTestProcedures()) {
                if (isTestProcedureDuplicate(dupResults, dto)) {
                    // Item already exists
                    String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedure",
                            certificationResult.getNumber(), dto.getEnteredName(), dto.getVersion());
                    dupResults.getMessages().add(warning);
                } else {
                    //Add the item to the final list
                    dupResults.getObjects().add(dto);
                }
            }
        }

        return dupResults;
    }

    private Boolean isTestProcedureDuplicate(
            DuplicateValidationResult<PendingCertificationResultTestProcedureDTO> dupResults,
            PendingCertificationResultTestProcedureDTO testProcedureDTO) {
        return dupResults.existsInObjects(testProcedureDTO, new Predicate<PendingCertificationResultTestProcedureDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestProcedureDTO dto2) {
                return testProcedureDTO.getEnteredName().equals(dto2.getEnteredName()) && testProcedureDTO.getVersion().equals(dto2.getVersion());
            }
        });
    }

    private DuplicateValidationResult<PendingCertificationResultTestDataDTO> removeDuplicateTestData(
            PendingCertificationResultDTO certificationResult) {

        DuplicateValidationResult<PendingCertificationResultTestDataDTO> dupResults =
                new DuplicateValidationResult<PendingCertificationResultTestDataDTO>();

        if (certificationResult.getTestData() != null) {
            for (PendingCertificationResultTestDataDTO dto : certificationResult.getTestData()) {
                if (isTestDataDuplicate(dupResults, dto)) {
                    // Item already exists
                    String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestData",
                            certificationResult.getNumber(), dto.getEnteredName(), dto.getVersion(),
                            dto.getAlteration());
                    dupResults.getMessages().add(warning);
                } else {
                    //Add the item to the final list
                    dupResults.getObjects().add(dto);
                }
            }
        }

        return dupResults;
    }

    private Boolean isTestDataDuplicate(
            DuplicateValidationResult<PendingCertificationResultTestDataDTO> dupResults,
            PendingCertificationResultTestDataDTO testDataDTO) {
        return dupResults.existsInObjects(testDataDTO, new Predicate<PendingCertificationResultTestDataDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestDataDTO dto2) {
                return testDataDTO.getEnteredName().equals(dto2.getEnteredName())
                        && testDataDTO.getVersion().equals(dto2.getVersion())
                        && testDataDTO.getAlteration().equals(dto2.getAlteration());
            }
        });
    }

    private static class DuplicateValidationResult<T> {
        private List<String> messages = new ArrayList<String>();
        private List<T> objects = new ArrayList<T>();

        public Boolean existsInObjects(T t, Predicate<T> predicate) {
            for (T item : objects) {
                if (predicate.test(item)) {
                    return true;
                }
            }
            return false;
        }

        public List<String> getMessages() {
            return messages;
        }

        public List<T> getObjects() {
            return objects;
        }
    }
}
