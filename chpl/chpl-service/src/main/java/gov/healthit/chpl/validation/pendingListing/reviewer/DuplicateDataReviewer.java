package gov.healthit.chpl.validation.pendingListing.reviewer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultAdditionalSoftwareDTO;
import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertificationResultTestToolDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;

@Component("pendingDuplicateDataReviewer")
public class DuplicateDataReviewer implements Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(DuplicateDataReviewer.class);

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        for (PendingCertificationResultDTO pcr : listing.getCertificationCriterion()) {
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
        }
    }

    private DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO> removeDuplicateAdditionalSoftware(
            PendingCertificationResultDTO certificationResult) {

        DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO> dupResults =
                new DuplicateValidationResult<PendingCertificationResultAdditionalSoftwareDTO>();

        for (PendingCertificationResultAdditionalSoftwareDTO dto : certificationResult.getAdditionalSoftware()) {

            Boolean result = dupResults.existsInObjects(dto,
                    new Predicate<PendingCertificationResultAdditionalSoftwareDTO>() {
                @Override
                public boolean test(PendingCertificationResultAdditionalSoftwareDTO dto2) {
                    return dto.getName().equals(dto2.getName()) && dto.getVersion().equals(dto2.getVersion());
                }
            }
                    );

            if (result) { // Item already exists
                dupResults.getMessages().add("We found a duplicate additional software");
            } else {
                dupResults.getObjects().add(dto);
            }
        }

        return dupResults;
    }

    private DuplicateValidationResult<PendingCertificationResultTestToolDTO> removeDuplicateTestTool(
            PendingCertificationResultDTO certificationResult) {

        DuplicateValidationResult<PendingCertificationResultTestToolDTO> dupResults =
                new DuplicateValidationResult<PendingCertificationResultTestToolDTO>();

        if (certificationResult.getTestTools() != null) {
            for (PendingCertificationResultTestToolDTO dto : certificationResult.getTestTools()) {
                Boolean result = dupResults.existsInObjects(dto,
                        new Predicate<PendingCertificationResultTestToolDTO>() {
                    @Override
                    public boolean test(PendingCertificationResultTestToolDTO dto2) {
                        return dto.getName().equals(dto2.getName()) && dto.getVersion().equals(dto2.getVersion());
                    }
                }
                        );

                if (result) { // Item already exists
                    dupResults.getMessages().add("We found a duplicate test tool");
                } else {
                    dupResults.getObjects().add(dto);
                }
            }
        }

        return dupResults;
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
