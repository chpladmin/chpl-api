package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.CertifiedProductUcdProcess;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;
import gov.healthit.chpl.upload.listing.ListingUploadHeadingUtil.Heading;
import jakarta.validation.ValidationException;
import lombok.extern.log4j.Log4j2;

@Component("sedUploadHandler")
@Log4j2
public class SedUploadHandler {
    private CertificationCriterionUploadHandler criterionHandler;
    private TestTaskUploadHandler testTaskHandler;
    private TestParticipantsUploadHandler testParticipantHandler;
    private UcdProcessUploadHandler ucdHandler;
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public SedUploadHandler(CertificationCriterionUploadHandler criterionHandler,
            TestTaskUploadHandler testTaskHandler,
            TestParticipantsUploadHandler testParticipantHandler,
            UcdProcessUploadHandler ucdHandler,
            ListingUploadHandlerUtil uploadUtil) {
        this.criterionHandler = criterionHandler;
        this.testTaskHandler = testTaskHandler;
        this.testParticipantHandler = testParticipantHandler;
        this.ucdHandler = ucdHandler;
        this.uploadUtil = uploadUtil;
    }

    public CertifiedProductSed parseAsSed(CSVRecord headingRecord, List<CSVRecord> listingRecords,
            CertifiedProductSearchDetails listing)
        throws ValidationException {
        List<TestTask> availableTestTasks = testTaskHandler.handle(headingRecord, listingRecords);
        List<String> allTaskIds = availableTestTasks.stream()
                .map(tt -> tt.getFriendlyId())
                .collect(Collectors.toList());
        List<TestParticipant> availableTestParticipants = testParticipantHandler.handle(headingRecord, listingRecords);
        List<String> allParticipantIds = availableTestParticipants.stream()
                .map(part -> part.getFriendlyId())
                .collect(Collectors.toList());
        List<TestTask> testTasks = new ArrayList<TestTask>();
        List<CertifiedProductUcdProcess> allUcdProcessesOnListing = new ArrayList<CertifiedProductUcdProcess>();

        int prevCertResultIndex = -1;
        int nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(0, headingRecord);
        while (nextCertResultIndex >= 0 && prevCertResultIndex != nextCertResultIndex) {
            List<CSVRecord> parsedCertResultRecords = uploadUtil.getCertificationResultRecordsFromIndex(
                    nextCertResultIndex, headingRecord, listingRecords);
            CSVRecord certHeadingRecord = uploadUtil.getHeadingRecord(parsedCertResultRecords);
            CertificationCriterion criterion = criterionHandler.handle(certHeadingRecord, listing);
            if (criterion != null) {
                List<CertifiedProductUcdProcess> certResultUcdProcesses = ucdHandler.handle(certHeadingRecord,
                        parsedCertResultRecords.subList(1, parsedCertResultRecords.size()));
                updateUcdProcessList(allUcdProcessesOnListing, certResultUcdProcesses, criterion);

                List<TestTask> certResultTestTasks = parseTestTaskIdsWithParticipantIds(
                        certHeadingRecord, parsedCertResultRecords.subList(1, parsedCertResultRecords.size()));
                updateTaskList(testTasks, certResultTestTasks, criterion, availableTestTasks, availableTestParticipants);
            }
            prevCertResultIndex = nextCertResultIndex;
            nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(
                    nextCertResultIndex + certHeadingRecord.size(), headingRecord);
        }

        CertifiedProductSed sed = CertifiedProductSed.builder()
                .testTasks(testTasks)
                .ucdProcesses(allUcdProcessesOnListing)
                .duplicateTestTaskIds(allTaskIds.stream()
                                .filter(tt -> Collections.frequency(allTaskIds, tt) > 1)
                                .collect(Collectors.toList()))
                .duplicateTestParticipantIds(allParticipantIds.stream()
                                .filter(tp -> Collections.frequency(allParticipantIds, tp) > 1)
                                .collect(Collectors.toList()))
            .build();
        updateUnusedTaskAndParticipantIds(sed, availableTestTasks, availableTestParticipants);
        return sed;
    }

    private void updateUcdProcessList(List<CertifiedProductUcdProcess> allUcdProcessesOnListing, List<CertifiedProductUcdProcess> certResultUcdProcesses,
            CertificationCriterion criterion) {
        certResultUcdProcesses.stream().forEach(certResultUcdProcess -> {
            if (listingContainsUcdProcess(allUcdProcessesOnListing, certResultUcdProcess)) {
                addCriteriaToExistingUcdProcess(allUcdProcessesOnListing, certResultUcdProcess, criterion);
            } else {
                LinkedHashSet<CertificationCriterion> criteriaSet = new LinkedHashSet<CertificationCriterion>();
                criteriaSet.add(criterion);
                certResultUcdProcess.setCriteria(criteriaSet);
                allUcdProcessesOnListing.add(certResultUcdProcess);
            }
        });
    }

    private boolean listingContainsUcdProcess(List<CertifiedProductUcdProcess> listingUcdProcesses, CertifiedProductUcdProcess certResultUcdProcess) {
        return listingUcdProcesses.stream()
            .filter(listingUcdProcess -> listingUcdProcess.getName()
                    .equals(certResultUcdProcess.getName()))
            .findAny().isPresent();
    }

    private void addCriteriaToExistingUcdProcess(List<CertifiedProductUcdProcess> listingUcdProcesses, CertifiedProductUcdProcess certResultUcdProcess,
            CertificationCriterion criterion) {
        listingUcdProcesses.stream()
            .filter(listingUcdProcess -> listingUcdProcess.getName()
                    .equals(certResultUcdProcess.getName()))
            .forEach(listingUcdProcess -> {
                listingUcdProcess.getCriteria().add(criterion);
            });
    }

    private void updateTaskList(List<TestTask> tasks, List<TestTask> certResultTasks, CertificationCriterion criterion,
            List<TestTask> availableTestTasks, List<TestParticipant> availableTestParticipants) {
        certResultTasks.stream().forEach(certResultTask -> {
            if (listingContainsTask(tasks, certResultTask)) {
                addCriteriaToExistingTestTask(tasks, certResultTask, criterion);
            } else {
                LinkedHashSet<CertificationCriterion> criteriaSet = new LinkedHashSet<CertificationCriterion>();
                criteriaSet.add(criterion);
                TestTask task = buildTestTaskFromAvailable(certResultTask, availableTestTasks, availableTestParticipants);
                task.setCriteria(criteriaSet);
                tasks.add(task);
            }
        });
    }

    private boolean listingContainsTask(List<TestTask> listingTestTasks, TestTask certResultTask) {
        return listingTestTasks.stream()
            .filter(listingTestTask -> Objects.equals(listingTestTask.getFriendlyId(), certResultTask.getFriendlyId()))
            .filter(listingTestTask ->
                participantsUniqueIdsMatch(listingTestTask.getTestParticipants(), certResultTask.getTestParticipants()))
            .findAny().isPresent();
    }

    private void addCriteriaToExistingTestTask(List<TestTask> listingTestTasks, TestTask certResultTask,
            CertificationCriterion criterion) {
        listingTestTasks.stream()
            .filter(listingTestTask -> Objects.equals(listingTestTask.getFriendlyId(), certResultTask.getFriendlyId()))
            .filter(listingTestTask ->
                participantsUniqueIdsMatch(listingTestTask.getTestParticipants(), certResultTask.getTestParticipants()))
            .forEach(listingTestTask -> {
                listingTestTask.getCriteria().add(criterion);
            });
    }

    private TestTask buildTestTaskFromAvailable(TestTask certResultTask,
            List<TestTask> availableTestTasks, List<TestParticipant> availableTestParticipants) {
        TestTask result = null;
        Optional<TestTask> taskFromAvailOpt = availableTestTasks.stream()
                .filter(availTask -> Objects.equals(availTask.getFriendlyId(), certResultTask.getFriendlyId()))
                .findAny();
        if (taskFromAvailOpt.isPresent()) {
            TestTask taskFromAvail = taskFromAvailOpt.get();
            result = taskFromAvail.toBuilder().build();
        } else {
            LOGGER.error("Could not find available test task with unique ID " + certResultTask.getFriendlyId());
            result = certResultTask.toBuilder().build();
        }

        LinkedHashSet<TestParticipant> builtParticipants = certResultTask.getTestParticipants().stream()
            .map(testParticipant -> buildTestParticipantFromAvailable(testParticipant, availableTestParticipants))
            .collect(Collectors.toCollection(LinkedHashSet::new));
        result.setTestParticipants(builtParticipants);
        return result;
    }

    private TestParticipant buildTestParticipantFromAvailable(TestParticipant testParticipant,
            List<TestParticipant> availableTestParticipants) {
        TestParticipant result = null;
        Optional<TestParticipant> participantFromAvailOpt = availableTestParticipants.stream()
            .filter(availParticipant -> Objects.equals(availParticipant.getFriendlyId(), testParticipant.getFriendlyId()))
            .findAny();
        if (participantFromAvailOpt.isPresent()) {
            TestParticipant participantFromAvail = participantFromAvailOpt.get();
            result = participantFromAvail.toBuilder().build();
        } else {
            result = testParticipant.toBuilder().build();
        }
        return result;
    }

    private boolean participantsUniqueIdsMatch(Set<TestParticipant> participantList1, Set<TestParticipant> participantList2) {
        List<String> participantUniqueIds1 = participantList1.stream().map(participant -> participant.getFriendlyId())
                .collect(Collectors.toList());
        List<String> participantUniqueIds2 = participantList2.stream().map(participant -> participant.getFriendlyId())
                .collect(Collectors.toList());
        Collections.sort(participantUniqueIds1);
        Collections.sort(participantUniqueIds2);
        return participantUniqueIds1.equals(participantUniqueIds2);
    }

    private List<TestTask> parseTestTaskIdsWithParticipantIds(
            CSVRecord certResultHeading, List<CSVRecord> certResultRecords) {
        List<TestTask> certResultTasks = new ArrayList<TestTask>();
        List<String> testTaskIds = parseTaskIds(certResultHeading, certResultRecords);
        List<String> testParticipantIds = parseParticipantIds(certResultHeading, certResultRecords);
        if (uploadUtil.areCollectionsEmpty(testTaskIds, testParticipantIds)) {
            return certResultTasks;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(testTaskIds)) {
            max = Math.max(max, testTaskIds.size());
        }
        if (CollectionUtils.isNotEmpty(testParticipantIds)) {
            max = Math.max(max, testParticipantIds.size());
        }

        IntStream.range(0, max)
                .forEachOrdered(index -> updateCertResultTasks(certResultTasks, index, testTaskIds, testParticipantIds));
        return certResultTasks;
    }

    private void updateCertResultTasks(List<TestTask> certResultTasks, int index, List<String> testTaskIds,
            List<String> testParticipantIds) {
        String testTaskId = (testTaskIds != null && testTaskIds.size() > index) ? testTaskIds.get(index) : null;
        String testParticipantIdsDelimited = (testParticipantIds != null && testParticipantIds.size() > index)
                ? testParticipantIds.get(index) : null;

        List<String> participantIds = new ArrayList<String>();
        if (!StringUtils.isEmpty(testParticipantIdsDelimited)) {
            String[] splitParticipantIds = testParticipantIdsDelimited.split(";");
            if (splitParticipantIds.length == 1) {
                splitParticipantIds = testParticipantIdsDelimited.split(",");
            }
            List<String> splitTrimmedParticipantIds = Arrays.stream(splitParticipantIds)
                    .map(String::trim)
                    .collect(Collectors.toList());
            participantIds.addAll(splitTrimmedParticipantIds);
        }

        TestTask certResultTask = TestTask.builder()
                .friendlyId(testTaskId)
                .testParticipants(participantIds.stream()
                        .map(participantId -> TestParticipant.builder().friendlyId(participantId).build())
                        .collect(Collectors.toCollection(LinkedHashSet::new)))
                .build();
        certResultTasks.add(certResultTask);
    }

    private void updateUnusedTaskAndParticipantIds(CertifiedProductSed sed,
            List<TestTask> availableTestTasks, List<TestParticipant> availableTestParticipants) {
        availableTestTasks.stream()
            .filter(availableTestTask -> !isReferenced(sed.getTestTasks(), availableTestTask))
            .forEach(unreferencedTestTask -> sed.getUnusedTestTaskUniqueIds().add(unreferencedTestTask.getFriendlyId()));
        availableTestParticipants.stream()
            .filter(availableTestParticipant -> !isReferenced(sed.getTestTasks(), availableTestParticipant))
            .forEach(unreferecedParticipant -> sed.getUnusedTestParticipantUniqueIds().add(unreferecedParticipant.getFriendlyId()));
    }

    private boolean isReferenced(List<TestTask> allTestTasks, TestTask testTaskToFind) {
        return allTestTasks.stream()
                .filter(tt -> Objects.equals(tt.getFriendlyId(), testTaskToFind.getFriendlyId()))
                .findAny().isPresent();
    }

    private boolean isReferenced(List<TestTask> allTestTasks, TestParticipant participantToFind) {
        return allTestTasks.stream()
                .flatMap(tt -> tt.getTestParticipants().stream())
                .filter(tp -> Objects.equals(tp.getFriendlyId(), participantToFind.getFriendlyId()))
                .findAny().isPresent();
    }

    private List<String> parseTaskIds(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseMultiRowFieldWithoutEmptyValues(Heading.TASK_ID, certHeadingRecord, certResultRecords);
    }

    private List<String> parseParticipantIds(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseMultiRowFieldWithoutEmptyValues(Heading.PARTICIPANT_ID, certHeadingRecord, certResultRecords);
    }
}
