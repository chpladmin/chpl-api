package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

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
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

@Component("pendingDuplicateData2015Reviewer")
public class DuplicateData2015Reviewer implements Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(DuplicateData2015Reviewer.class);

    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public DuplicateData2015Reviewer(ErrorMessageUtil errorMessageUtil) {
        this.errorMessageUtil = errorMessageUtil;
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
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
            final PendingCertificationResultDTO certificationResult) {

        DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO> dupResults =
                new DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO>();

        if (certificationResult.getAdditionalSoftware() != null) {
            for (PendingCertificationResultAdditionalSoftwareDTO dto : certificationResult.getAdditionalSoftware()) {
                if (isAdditionalSoftwareDuplicate(dupResults, dto)) {
                    // Item already exists
                    String warning = "";
                    if (dto.getChplId() != null && dto.getGrouping() != null) {
                        warning = errorMessageUtil.getMessage("listing.criteria.duplicateAdditionalSoftwareCP.2015",
                                certificationResult.getNumber(), dto.getChplId(), dto.getGrouping());
                    } else if (dto.getName() != null && dto.getVersion() != null
                            && dto.getGrouping() != null) {
                        warning = errorMessageUtil.getMessage("listing.criteria.duplicateAdditionalSoftwareNonCP.2015",
                                certificationResult.getNumber(), dto.getName(), dto.getVersion(), dto.getGrouping());
                    }
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
            final DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO> dupResults,
            final PendingCertificationResultAdditionalSoftwareDTO additionalSoftwareDTO) {

        return dupResults.existsInObjects(additionalSoftwareDTO,
                new Predicate<PendingCertificationResultAdditionalSoftwareDTO>() {
            @Override
            public boolean test(PendingCertificationResultAdditionalSoftwareDTO dto2) {
                if (additionalSoftwareDTO.getChplId() != null && dto2.getChplId() != null
                        && additionalSoftwareDTO.getGrouping() != null && dto2.getGrouping() != null) {

                    return additionalSoftwareDTO.getChplId().equals(dto2.getChplId())
                            && additionalSoftwareDTO.getGrouping().equals(dto2.getGrouping());

                } else if (additionalSoftwareDTO.getName() != null && dto2.getName() != null
                        && additionalSoftwareDTO.getVersion() != null && dto2.getVersion()!= null
                        && additionalSoftwareDTO.getGrouping() != null && dto2.getGrouping()!= null) {

                    return additionalSoftwareDTO.getName().equals(dto2.getName())
                            && additionalSoftwareDTO.getVersion().equals(dto2.getVersion())
                            && additionalSoftwareDTO.getGrouping().equals(dto2.getGrouping());
                } else {
                    return false;
                }
            }
        });
    }

    private DuplicateValidationResult<PendingCertificationResultTestToolDTO> removeDuplicateTestTool(
            final PendingCertificationResultDTO certificationResult) {

        DuplicateValidationResult<PendingCertificationResultTestToolDTO> dupResults =
                new DuplicateValidationResult<PendingCertificationResultTestToolDTO>();

        if (certificationResult.getTestTools() != null) {
            for (PendingCertificationResultTestToolDTO dto : certificationResult.getTestTools()) {
                if (isTestToolDuplicate(dupResults, dto)) {
                    // Item already exists
                    String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestTool.2015",
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

    private Boolean isTestToolDuplicate(
            final DuplicateValidationResult<PendingCertificationResultTestToolDTO> dupResults,
            final PendingCertificationResultTestToolDTO testToolDTO) {
        return dupResults.existsInObjects(testToolDTO, new Predicate<PendingCertificationResultTestToolDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestToolDTO dto2) {
                if (testToolDTO.getName() != null && dto2.getName() != null
                        && testToolDTO.getVersion() != null && dto2.getVersion() != null) {
                    return testToolDTO.getName().equals(dto2.getName())
                            && testToolDTO.getVersion().equals(dto2.getVersion());
                } else {
                    return false;
                }
            }
        });
    }

    private DuplicateValidationResult<PendingCertificationResultTestProcedureDTO> removeDuplicateTestProcedure(
            final PendingCertificationResultDTO certificationResult) {

        DuplicateValidationResult<PendingCertificationResultTestProcedureDTO> dupResults =
                new DuplicateValidationResult<PendingCertificationResultTestProcedureDTO>();

        if (certificationResult.getTestProcedures() != null) {
            for (PendingCertificationResultTestProcedureDTO dto : certificationResult.getTestProcedures()) {
                if (isTestProcedureDuplicate(dupResults, dto)) {
                    // Item already exists
                    String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestProcedure.2015",
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
            final DuplicateValidationResult<PendingCertificationResultTestProcedureDTO> dupResults,
            final PendingCertificationResultTestProcedureDTO testProcedureDTO) {
        return dupResults.existsInObjects(testProcedureDTO, new Predicate<PendingCertificationResultTestProcedureDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestProcedureDTO dto2) {

                if (testProcedureDTO.getEnteredName() != null && dto2.getEnteredName() != null
                        && testProcedureDTO.getVersion() != null && dto2.getVersion() != null) {
                    return testProcedureDTO.getEnteredName().equals(dto2.getEnteredName())
                            && testProcedureDTO.getVersion().equals(dto2.getVersion());
                } else {
                    return false;
                }
            }
        });
    }

    private DuplicateValidationResult<PendingCertificationResultTestDataDTO> removeDuplicateTestData(
            final PendingCertificationResultDTO certificationResult) {

        DuplicateValidationResult<PendingCertificationResultTestDataDTO> dupResults =
                new DuplicateValidationResult<PendingCertificationResultTestDataDTO>();

        if (certificationResult.getTestData() != null) {
            for (PendingCertificationResultTestDataDTO dto : certificationResult.getTestData()) {
                if (isTestDataDuplicate(dupResults, dto)) {
                    // Item already exists
                    String warning = errorMessageUtil.getMessage("listing.criteria.duplicateTestData.2015",
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
            final DuplicateValidationResult<PendingCertificationResultTestDataDTO> dupResults,
            final PendingCertificationResultTestDataDTO testDataDTO) {
        return dupResults.existsInObjects(testDataDTO, new Predicate<PendingCertificationResultTestDataDTO>() {
            @Override
            public boolean test(PendingCertificationResultTestDataDTO dto2) {
                if (testDataDTO.getEnteredName() != null && dto2.getEnteredName() != null
                        && testDataDTO.getVersion() != null && dto2.getVersion() != null
                        && testDataDTO.getAlteration() != null && dto2.getAlteration() != null) {

                    return testDataDTO.getEnteredName().equals(dto2.getEnteredName())
                            && testDataDTO.getVersion().equals(dto2.getVersion())
                            && testDataDTO.getAlteration().equals(dto2.getAlteration());
                } else {
                    return false;
                }
            }
        });
    }

    private static class DuplicateValidationResult<T> {
        private List<String> messages = new ArrayList<String>();
        private List<T> objects = new ArrayList<T>();

        public Boolean existsInObjects(final T t, final Predicate<T> predicate) {
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
