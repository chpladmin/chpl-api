package gov.healthit.chpl.scheduler.job.onetime.participants;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.persistence.Query;
import javax.transaction.Transactional;

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
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "updateParticipantsJobLogger")
public class ReprocessFromUploadedCsvStrategy implements ParticipantUpdateStrategy {
    private ProcessedListingUploadManager processedListingUploadManager;

    @Autowired
    public ReprocessFromUploadedCsvStrategy(ProcessedListingUploadManager processedListingUploadManager) {
        this.processedListingUploadManager = processedListingUploadManager;
    }

    public boolean updateParticipants(CertifiedProductSearchDetails confirmedListing) {
        CertifiedProductSearchDetails uploadedListing =
                processedListingUploadManager.getUploadedDetailsByConfirmedCertifiedProductId(confirmedListing.getId());
        if (hasSedErrors(uploadedListing)) {
            printListingErrorsAndWarnings(uploadedListing);
            return false;
        }

        //there should be the same test tasks present in both listings
        List<TestTask> taskDiff1 = subtractTestTasks(confirmedListing.getSed().getTestTasks(), uploadedListing.getSed().getTestTasks());
        List<TestTask> taskDiff2 = subtractTestTasks(uploadedListing.getSed().getTestTasks(), confirmedListing.getSed().getTestTasks());
        if (!CollectionUtils.isEmpty(taskDiff1)) {
            LOGGER.warn("A test task was found in the confirmed listing that is not in the uploaded listing.");
            LOGGER.warn(taskDiff1.stream().map(task -> task.toString()).collect(Collectors.joining(System.lineSeparator())));
            return false;
        } else if (!CollectionUtils.isEmpty(taskDiff2)) {
            LOGGER.warn("A test task was found in the uploaded listing that is not in the confirmed listing.");
            LOGGER.warn(taskDiff2.stream().map(task -> task.toString()).collect(Collectors.joining(System.lineSeparator())));
            return false;
        }

        //there should be the same test participants present in both listings
        List<TestParticipant> confirmedTestParticipants = confirmedListing.getSed().getTestTasks().stream()
                .flatMap(task -> task.getTestParticipants().stream())
                .collect(Collectors.toList());
        List<TestParticipant> uploadedTestParticipants = uploadedListing.getSed().getTestTasks().stream()
                .flatMap(task -> task.getTestParticipants().stream())
                .collect(Collectors.toList());
        List<TestParticipant> participantDiff1 = subtractTestParticipants(confirmedTestParticipants, uploadedTestParticipants);
        List<TestParticipant> participantDiff2 = subtractTestParticipants(uploadedTestParticipants, confirmedTestParticipants);

        if (!CollectionUtils.isEmpty(participantDiff1)) {
            LOGGER.warn("A test participant was found in the confirmed listing that is not in the uploaded listing.");
            LOGGER.warn(participantDiff1.stream().map(participant -> participant.toString()).collect(Collectors.joining(System.lineSeparator())));
            return false;
        } else if (!CollectionUtils.isEmpty(participantDiff2)) {
            LOGGER.warn("A test participant was found in the uploaded listing that is not in the confirmed listing.");
            LOGGER.warn(participantDiff2.stream().map(participant -> participant.toString()).collect(Collectors.joining(System.lineSeparator())));
            return false;
        }

        //if we haven't returned yet, we think this listing is eligible for re-processing
        boolean allTestTasksCanBeUpdated = true;
        Iterator<TestTask> confirmedTaskIter = confirmedListing.getSed().getTestTasks().iterator();
        while (confirmedTaskIter.hasNext() && allTestTasksCanBeUpdated) {
            TestTask confirmedTestTask = confirmedTaskIter.next();
            Optional<TestTask> uploadedTestTask = findTestTask(confirmedTestTask, uploadedListing.getSed().getTestTasks());
            if (uploadedTestTask.isEmpty()) {
                //we really shouldn't get here due to the checks above that compare lists of tasks
                LOGGER.warn("Confirmed test task with ID " + confirmedTestTask.getId() + " cannot be re-processed. No matching uploaded test task was found.");
                allTestTasksCanBeUpdated = false;
            } else {
                //compare the participants for the uploaded task to the confirmed task to make sure
                //they all have the exact same values
                List<TestParticipant> taskParticipantsDiff1 = subtractTestParticipants(confirmedTestTask.getTestParticipants(), uploadedTestTask.get().getTestParticipants());
                if (!CollectionUtils.isEmpty(taskParticipantsDiff1)) {
                    LOGGER.warn("A test participant was found in the confirmed test task " + confirmedTestTask.getId()
                        + " that is not in the uploaded task.");
                    LOGGER.warn(taskParticipantsDiff1.stream().map(participant -> participant.toString()).collect(Collectors.joining(System.lineSeparator())));
                    allTestTasksCanBeUpdated = false;
                }
                List<TestParticipant> taskParticipantsDiff2 = subtractTestParticipants(uploadedTestTask.get().getTestParticipants(), confirmedTestTask.getTestParticipants());
                if (!CollectionUtils.isEmpty(taskParticipantsDiff2)) {
                    LOGGER.warn("A test participant was found in the uploaded task that was not found in the "
                            + "confirmed test task " + confirmedTestTask.getId());
                    LOGGER.warn(taskParticipantsDiff2.stream().map(participant -> participant.toString()).collect(Collectors.joining(System.lineSeparator())));
                    allTestTasksCanBeUpdated = false;
                }

                if (CollectionUtils.isEmpty(taskParticipantsDiff1) && CollectionUtils.isEmpty(taskParticipantsDiff2)) {
                    //if we are still processing, then this confirmed test task and this uploaded test task have participants with the same values
                    confirmedTestTask.setTestParticipants(uploadedTestTask.get().getTestParticipants());
                }
            }
        }
        return allTestTasksCanBeUpdated;
    }

    private boolean hasSedErrors(CertifiedProductSearchDetails listing) {
        //there are probably errors and warnings that weren't here originally -
        //some criteria have been removed, we changed measure parsing, etc
        return !CollectionUtils.isEmpty(listing.getErrorMessages())
             && isAnyMessageAboutSed(listing.getErrorMessages());
    }

    private boolean isAnyMessageAboutSed(Collection<String> messages) {
        return messages.stream()
            .map(msg -> msg.toUpperCase())
            .filter(upperCaseMsg -> upperCaseMsg.contains("SED")
                    || upperCaseMsg.contains("TASK")
                    || upperCaseMsg.contains("PARTICIPANT"))
            .count() > 0;
    }

    private void printListingErrorsAndWarnings(CertifiedProductSearchDetails listing) {
        LOGGER.info("Errors for listing: " + listing.getChplProductNumber() + " (confirmed listing " + listing.getId() + ")");
        //there might be errors due to measure parsing since all of these files had the old measure columns...
        if (CollectionUtils.isEmpty(listing.getErrorMessages())) {
            LOGGER.info("\t0 errors.");
        } else {
            listing.getErrorMessages().stream()
                .forEach(msg -> LOGGER.info("\t" + msg));
        }

        LOGGER.info("Warnings for listing: " + listing.getChplProductNumber() + " (confirmed listing " + listing.getId() + ")");
        if (CollectionUtils.isEmpty(listing.getWarningMessages())) {
            LOGGER.info("\t0 warnings.");
        } else {
            listing.getWarningMessages().stream()
                .forEach(msg -> LOGGER.info("\t" + msg));
        }
    }

    private List<TestTask> subtractTestTasks(Collection<TestTask> tasksFromA, Collection<TestTask> tasksFromB) {
        Predicate<TestTask> notInListB = taskFromA -> !tasksFromB.stream()
                .anyMatch(taskFromB -> testTaskMatches(taskFromA, taskFromB));

        return tasksFromA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private List<TestParticipant> subtractTestParticipants(Collection<TestParticipant> participantsFromA, Collection<TestParticipant> participantsFromB) {
        Predicate<TestParticipant> notInListB = participantFromA -> !participantsFromB.stream()
                .anyMatch(participantFromB -> testParticipantMatches(participantFromA, participantFromB));

        return participantsFromA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private Optional<TestTask> findTestTask(TestTask task, List<TestTask> tasksToSearch) {
        return tasksToSearch.stream()
                .filter(currTask -> testTaskMatches(task, currTask))
                .findAny();
    }

    private boolean testTaskMatches(TestTask testTask1, TestTask testTask2) {
        boolean result = false;
        if (StringUtils.equals(testTask1.getDescription(), testTask2.getDescription())
                && Objects.equals(testTask1.getTaskErrors(), testTask2.getTaskErrors())
                && Objects.equals(testTask1.getTaskErrorsStddev(), testTask2.getTaskErrorsStddev())
                && Objects.equals(testTask1.getTaskPathDeviationObserved(), testTask2.getTaskPathDeviationObserved())
                && Objects.equals(testTask1.getTaskPathDeviationOptimal(), testTask2.getTaskPathDeviationOptimal())
                && Objects.equals(testTask1.getTaskRating(), testTask2.getTaskRating())
                && StringUtils.equals(testTask1.getTaskRatingScale(), testTask2.getTaskRatingScale())
                && Objects.equals(testTask1.getTaskRatingStddev(), testTask2.getTaskRatingStddev())
                && Objects.equals(testTask1.getTaskSuccessAverage(), testTask2.getTaskSuccessAverage())
                && Objects.equals(testTask1.getTaskSuccessStddev(), testTask2.getTaskSuccessStddev())
                && Objects.equals(testTask1.getTaskTimeAvg(), testTask2.getTaskTimeAvg())
                && Objects.equals(testTask1.getTaskTimeDeviationObservedAvg(),
                        testTask2.getTaskTimeDeviationObservedAvg())
                && Objects.equals(testTask1.getTaskTimeDeviationOptimalAvg(),
                        testTask2.getTaskTimeDeviationOptimalAvg())
                && Objects.equals(testTask1.getTaskTimeStddev(), testTask2.getTaskTimeStddev())) {
            result = true;
        }
        return result;
    }

    private boolean testParticipantMatches(TestParticipant testParticipant1, TestParticipant testParticipant2) {
        boolean result = false;
        if (StringUtils.equals(testParticipant1.getAgeRange(), testParticipant2.getAgeRange())
                && Objects.equals(testParticipant1.getAgeRangeId(), testParticipant2.getAgeRangeId())
                && StringUtils.equals(testParticipant1.getAssistiveTechnologyNeeds(),
                        testParticipant2.getAssistiveTechnologyNeeds())
                && Objects.equals(testParticipant1.getComputerExperienceMonths(),
                        testParticipant2.getComputerExperienceMonths())
                && StringUtils.equals(testParticipant1.getEducationTypeName(), testParticipant2.getEducationTypeName())
                && Objects.equals(testParticipant1.getEducationTypeId(), testParticipant2.getEducationTypeId())
                && StringUtils.equals(testParticipant1.getGender(), testParticipant2.getGender())
                && StringUtils.equals(testParticipant1.getOccupation(), testParticipant2.getOccupation())
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
