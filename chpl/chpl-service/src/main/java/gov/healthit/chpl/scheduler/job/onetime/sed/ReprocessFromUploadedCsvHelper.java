package gov.healthit.chpl.scheduler.job.onetime.sed;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpload;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.upload.listing.ListingUploadEntity;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.handler.ListingDetailsUploadHandler;
import gov.healthit.chpl.upload.listing.normalizer.ListingDetailsNormalizer;
import gov.healthit.chpl.validation.listing.Edition2015ListingValidator;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "updatedSedFriendlyIdsJobLogger")
public class ReprocessFromUploadedCsvHelper {
    private ProcessedListingUploadManager processedListingUploadManager;

    @Autowired
    public ReprocessFromUploadedCsvHelper(ProcessedListingUploadManager processedListingUploadManager) {
        this.processedListingUploadManager = processedListingUploadManager;
    }

    public boolean updateTasks(CertifiedProductSearchDetails currentListing, List<CertifiedProductSearchDetails> listingsWithUploadFilesWithDuplicates) {
        CertifiedProductSearchDetails uploadedListing =
                processedListingUploadManager.getUploadedDetailsByConfirmedCertifiedProductId(currentListing.getId());
        if (hasSedErrors(uploadedListing)) {
            printListingErrorsAndWarnings(uploadedListing);
            return false;
        }
        if (isAnyMessageAboutDuplicateTasksOrParticipants(uploadedListing.getErrorMessages().castToCollection())) {
            listingsWithUploadFilesWithDuplicates.add(uploadedListing);
        }

        int numTasksUpdated = 0;
        Iterator<TestTask> currentTaskIter = currentListing.getSed().getTestTasks().iterator();
        while (currentTaskIter.hasNext()) {
            TestTask currentTestTask = currentTaskIter.next();
            if (StringUtils.isEmpty(currentTestTask.getFriendlyId())) {
                if (!hasAnyDuplicates(currentTestTask, currentListing.getSed().getTestTasks())) {
                    Optional<TestTask> uploadedTestTask = findTestTask(currentTestTask, uploadedListing.getSed().getTestTasks());
                    if (uploadedTestTask.isPresent()) {
                        if (!hasAnyDuplicates(uploadedTestTask.get(), uploadedListing.getSed().getTestTasks())) {
                            //if we are still processing, then this confirmed test task and this uploaded test task have the same values
                            //and there are no other test tasks with these values
                            LOGGER.info("Setting friendly ID to " + uploadedTestTask.get().getFriendlyId() + " for test task " + currentTestTask.getId());
                            currentTestTask.setFriendlyId(uploadedTestTask.get().getFriendlyId());
                            numTasksUpdated++;
                        } else {
                            LOGGER.info("Test task with ID " + uploadedTestTask.get().getFriendlyId() + " has the same data as at least one other test task in the uploaded file. Skipping.");
                        }
                    } else {
                        LOGGER.warn("Test task with ID " + currentTestTask.getId() + " cannot be re-processed. No matching uploaded test task was found.");
                    }
                } else {
                    LOGGER.warn("Test task with ID " + currentTestTask.getId() + " has the same data as at least one other test task in listing " + currentListing.getId() + ". Skipping.");
                }
            } else {
                LOGGER.info("Test task with ID " + currentTestTask.getId() + " already has a friendly ID. Skipping.");
            }
        }
        return numTasksUpdated > 0;
    }

    public boolean updateParticipants(CertifiedProductSearchDetails currentListing) {
        CertifiedProductSearchDetails uploadedListing =
                processedListingUploadManager.getUploadedDetailsByConfirmedCertifiedProductId(currentListing.getId());
        if (hasSedErrors(uploadedListing)) {
            printListingErrorsAndWarnings(uploadedListing);
            return false;
        }

        int numParticipantsUpdated = 0;
        List<TestParticipant> allCurrentParticipants = currentListing.getSed().getTestTasks().stream()
                .flatMap(tt -> tt.getTestParticipants().stream())
                .collect(Collectors.toList());

        List<TestParticipant> uploadedTestParticipants = uploadedListing.getSed().getTestTasks().stream()
                .flatMap(tt -> tt.getTestParticipants().stream())
                .collect(Collectors.toList());

        Iterator<TestParticipant> currentParticipantsIter = allCurrentParticipants.iterator();
        while (currentParticipantsIter.hasNext()) {
            TestParticipant currentParticipant = currentParticipantsIter.next();
            if (StringUtils.isEmpty(currentParticipant.getFriendlyId())) {
                if (!hasAnyDuplicates(currentParticipant, allCurrentParticipants)) {
                    Optional<TestParticipant> uploadedTestParticipant = findTestParticipant(currentParticipant, uploadedTestParticipants);
                    if (uploadedTestParticipant.isPresent()) {
                        //if we are still processing, then this confirmed participant and this uploaded participant have the same values
                        //and there are no other participants with these values
                        LOGGER.info("Setting friendly ID to " + uploadedTestParticipant.get().getFriendlyId() + " for test participant " + currentParticipant.getId());
                        currentParticipant.setFriendlyId(uploadedTestParticipant.get().getFriendlyId());
                        numParticipantsUpdated++;
                    } else {
                        LOGGER.warn("No test participant was found in the upload file matching " + currentParticipant.toString());
                    }
                } else {
                    LOGGER.warn("Test participant with ID " + currentParticipant.getId() + " has the same data as at least one other participant in listing " + currentListing.getId() + ". Skipping.");
                }
            } else {
                LOGGER.info("Test participant with ID " + currentParticipant.getId() + " already has a friendly ID. Skipping.");
            }
        }
        return numParticipantsUpdated > 0;
    }

    private boolean hasSedErrors(CertifiedProductSearchDetails listing) {
        //there are probably errors and warnings that weren't here originally -
        //some criteria have been removed, we changed measure parsing, etc
        return !CollectionUtils.isEmpty(listing.getErrorMessages().castToCollection())
             && isAnyMessageAboutSed(listing.getErrorMessages().castToCollection());
    }

    private boolean isAnyMessageAboutSed(Collection<String> messages) {
        return messages.stream()
            .map(msg -> msg.toUpperCase())
            //the code to update friendly ids handles duplicate identifiers in the files so we don't need to care about the
            //"duplicate" errors
            .filter(upperCaseMsg -> !upperCaseMsg.contains("Test Task Identifiers must be unique across all tasks.".toUpperCase()))
            .filter(upperCaseMsg -> !upperCaseMsg.contains("Participant Identifiers must be unique across all participants.".toUpperCase()))
            //check for any other task or participant-related error
            .filter(upperCaseMsg -> upperCaseMsg.contains("SED")
                    || upperCaseMsg.contains("TASK")
                    || upperCaseMsg.contains("PARTICIPANT"))
            .count() > 0;
    }

    private boolean isAnyMessageAboutDuplicateTasksOrParticipants(Collection<String> messages) {
        return messages.stream()
            .map(msg -> msg.toUpperCase())
            .filter(upperCaseMsg -> upperCaseMsg.contains("Test Task Identifiers must be unique across all tasks.".toUpperCase())
                    || upperCaseMsg.contains("Participant Identifiers must be unique across all participants.".toUpperCase()))
            .count() > 0;
    }

    private void printListingErrorsAndWarnings(CertifiedProductSearchDetails listing) {
        LOGGER.info("Errors for listing: " + listing.getChplProductNumber() + " (confirmed listing " + listing.getId() + ")");
        //there might be errors due to measure parsing since all of these files had the old measure columns...
        if (CollectionUtils.isEmpty(listing.getErrorMessages().castToCollection())) {
            LOGGER.info("\t0 errors.");
        } else {
            listing.getErrorMessages().stream()
                .forEach(msg -> LOGGER.info("\t" + msg));
        }

        LOGGER.info("Warnings for listing: " + listing.getChplProductNumber() + " (confirmed listing " + listing.getId() + ")");
        if (CollectionUtils.isEmpty(listing.getWarningMessages().castToCollection())) {
            LOGGER.info("\t0 warnings.");
        } else {
            listing.getWarningMessages().stream()
                .forEach(msg -> LOGGER.info("\t" + msg));
        }
    }

    private boolean hasAnyDuplicates(TestTask task, List<TestTask> tasksToSearch) {
        return tasksToSearch.stream()
                .filter(currTask -> testTaskMatches(task, currTask))
                .count() > 1;
    }

    private boolean hasAnyDuplicates(TestParticipant participant, List<TestParticipant> participantsToSearch) {
        return participantsToSearch.stream()
                .filter(currParticipant -> testParticipantMatches(participant, currParticipant))
                .filter(matchedParticipant -> !matchedParticipant.getId().equals(participant.getId()))
                .count() > 1;
    }

    private Optional<TestTask> findTestTask(TestTask task, List<TestTask> tasksToSearch) {
        return tasksToSearch.stream()
                .filter(currTask -> testTaskMatches(task, currTask))
                .findAny();
    }

    private boolean testTaskMatches(TestTask testTask1, TestTask testTask2) {
        boolean result = false;
        if (StringUtils.equalsIgnoreCase(StringUtils.normalizeSpace(testTask1.getDescription()),
                StringUtils.normalizeSpace(testTask2.getDescription()))
                && Objects.equals(testTask1.getTaskErrors(), testTask2.getTaskErrors())
                && Objects.equals(testTask1.getTaskErrorsStddev(), testTask2.getTaskErrorsStddev())
                && Objects.equals(testTask1.getTaskPathDeviationObserved(), testTask2.getTaskPathDeviationObserved())
                && Objects.equals(testTask1.getTaskPathDeviationOptimal(), testTask2.getTaskPathDeviationOptimal())
                && Objects.equals(testTask1.getTaskRating(), testTask2.getTaskRating())
                && StringUtils.equalsIgnoreCase(StringUtils.normalizeSpace(testTask1.getTaskRatingScale()),
                        StringUtils.normalizeSpace(testTask2.getTaskRatingScale()))
                && Objects.equals(testTask1.getTaskRatingStddev(), testTask2.getTaskRatingStddev())
                && Objects.equals(testTask1.getTaskSuccessAverage(), testTask2.getTaskSuccessAverage())
                && Objects.equals(testTask1.getTaskSuccessStddev(), testTask2.getTaskSuccessStddev())
                && Objects.equals(testTask1.getTaskTimeAvg(), testTask2.getTaskTimeAvg())
                && Objects.equals(testTask1.getTaskTimeDeviationObservedAvg(),
                        testTask2.getTaskTimeDeviationObservedAvg())
                && Objects.equals(testTask1.getTaskTimeDeviationOptimalAvg(),
                        testTask2.getTaskTimeDeviationOptimalAvg())
                && Objects.equals(testTask1.getTaskTimeStddev(), testTask2.getTaskTimeStddev())
                && testTask1.getTestParticipants().size() == testTask2.getTestParticipants().size()) {
            result = true;
        }
        return result;
    }

    private Optional<TestParticipant> findTestParticipant(TestParticipant participant, List<TestParticipant> participantsToSearch) {
        return participantsToSearch.stream()
                .filter(currParticipant -> testParticipantMatches(participant, currParticipant))
                .findAny();
    }

    private boolean testParticipantMatches(TestParticipant testParticipant1, TestParticipant testParticipant2) {
        boolean result = false;
        if (StringUtils.equalsIgnoreCase(testParticipant1.getAge().getName(), testParticipant2.getAge().getName())
                && Objects.equals(testParticipant1.getAge().getId(), testParticipant2.getAge().getId())
                && StringUtils.equalsIgnoreCase(testParticipant1.getAssistiveTechnologyNeeds(),
                        testParticipant2.getAssistiveTechnologyNeeds())
                && Objects.equals(testParticipant1.getComputerExperienceMonths(),
                        testParticipant2.getComputerExperienceMonths())
                && StringUtils.equalsIgnoreCase(testParticipant1.getEducationType().getName(), testParticipant2.getEducationType().getName())
                && Objects.equals(testParticipant1.getEducationType().getId(), testParticipant2.getEducationType().getId())
                && StringUtils.equalsIgnoreCase(testParticipant1.getGender(), testParticipant2.getGender())
                && StringUtils.equalsIgnoreCase(testParticipant1.getOccupation(), testParticipant2.getOccupation())
                && Objects.equals(testParticipant1.getProductExperienceMonths(),
                        testParticipant2.getProductExperienceMonths())
                && Objects.equals(testParticipant1.getProfessionalExperienceMonths(),
                        testParticipant2.getProfessionalExperienceMonths())) {
            result = true;
        }
        return result;
    }

    @Component
    @NoArgsConstructor
    @Log4j2(topic = "updateParticipantsJobLogger")
    private static class ProcessedListingUploadDao extends BaseDAOImpl {
        private static final String GET_PROCESSED_ENTITY_HQL_BEGIN = "SELECT ul "
                + "FROM ListingUploadEntity ul "
                + "LEFT JOIN FETCH ul.certificationBody acb "
                + "LEFT JOIN FETCH acb.address "
                + "WHERE ul.certifiedProductId IS NOT NULL ";

        public ListingUpload getByConfirmedCertifiedProductIdIncludingRecords(Long certifiedProductId) {
            Query query = entityManager.createQuery(GET_PROCESSED_ENTITY_HQL_BEGIN
                    + " AND ul.certifiedProductId = :certifiedProductId ", ListingUploadEntity.class);
            query.setParameter("certifiedProductId", certifiedProductId);
            List<ListingUploadEntity> entities = query.getResultList();
            if (entities == null || entities.size() == 0) {
                LOGGER.error("No processed uploaded listing found with certified product ID " + certifiedProductId);
                return null;
            }
            return entities.get(0).toDomainWithRecords();
        }
    }

    @Component
    @NoArgsConstructor
    @Log4j2(topic = "updateParticipantsJobLogger")
    private static class ProcessedListingUploadManager {

        private ListingDetailsNormalizer listingNormalizer;
        private ListingUploadHandlerUtil uploadUtil;
        private ProcessedListingUploadDao processedListingUploadDao;
        //use this validator instead of the ListingUploadValidator because
        //otherwise we get an error about "existing listing with this CHPL Product Number"
        private Edition2015ListingValidator listingValidator;
        private ListingDetailsUploadHandler listingDetailsHandler;

        @Autowired
        ProcessedListingUploadManager(ListingDetailsNormalizer listingNormalizer,
                Edition2015ListingValidator listingValidator,
                ListingDetailsUploadHandler listingDetailsHandler,
                ProcessedListingUploadDao processedListingUploadDao,
                ListingUploadHandlerUtil uploadUtil) {
            this.listingNormalizer = listingNormalizer;
            this.listingValidator = listingValidator;
            this.listingDetailsHandler = listingDetailsHandler;
            this.processedListingUploadDao = processedListingUploadDao;
            this.uploadUtil = uploadUtil;
        }

        @Transactional
        public CertifiedProductSearchDetails getUploadedDetailsByConfirmedCertifiedProductId(Long id) {
            ListingUpload listingUpload = processedListingUploadDao.getByConfirmedCertifiedProductIdIncludingRecords(id);
            LOGGER.info("Got listing upload with ID " + id);
            List<CSVRecord> allCsvRecords = listingUpload.getRecords();
            if (allCsvRecords == null) {
                LOGGER.debug("Listing upload with ID " + id + " has no CSV records associated with it.");
                return null;
            }
            LOGGER.trace("Listing upload with ID " + id + " has " + allCsvRecords.size() + " CSV records associated with it.");
            int headingRowIndex = uploadUtil.getHeadingRecordIndex(allCsvRecords);
            CSVRecord headingRecord = uploadUtil.getHeadingRecord(allCsvRecords);
            List<CSVRecord> allListingRecords = allCsvRecords.subList(headingRowIndex + 1, allCsvRecords.size());
            LOGGER.trace("Converting listing upload with ID " + id + " into CertifiedProductSearchDetails object");
            CertifiedProductSearchDetails listing =
                    listingDetailsHandler.parseAsListing(headingRecord, allListingRecords);
            LOGGER.trace("Converted listing upload with ID " + id + " into CertifiedProductSearchDetails object");
            listingNormalizer.normalize(listing);
            listing.setId(id);
            LOGGER.trace("Normalized listing upload with ID " + id);
            listingValidator.validate(listing);
            LOGGER.trace("Validated listing upload with ID " + id);
            listing.setId(listingUpload.getCertifiedProductId());
            return listing;
        }
    }
}