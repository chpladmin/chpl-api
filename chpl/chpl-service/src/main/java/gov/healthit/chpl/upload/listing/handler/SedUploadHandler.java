package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.ValidationException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
import gov.healthit.chpl.upload.listing.Headings;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("sedUploadHandler")
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

    public CertifiedProductSed parseAsSed(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        List<TestTask> availableTestTasks = testTaskHandler.handle(headingRecord, listingRecords);
        List<TestParticipant> availableTestParticipants = testParticipantHandler.handle(headingRecord, listingRecords);
        List<TestTask> testTasks = new ArrayList<TestTask>();
        List<UcdProcess> ucdProcesses = new ArrayList<UcdProcess>();

        int nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(0, headingRecord);
        while (nextCertResultIndex >= 0) {
            List<CSVRecord> parsedCertResultRecords = uploadUtil.getCertificationResultRecordsFromIndex(
                    nextCertResultIndex, headingRecord, listingRecords);
            CSVRecord certHeadingRecord = uploadUtil.getHeadingRecord(parsedCertResultRecords);
            CertificationCriterion criterion = criterionHandler.handle(certHeadingRecord);
            if (criterion != null) {
                List<UcdProcess> certResultUcdProcesses = ucdHandler.handle(certHeadingRecord,
                        parsedCertResultRecords.subList(0, parsedCertResultRecords.size()));
                //TODO: add each ucd process to ucdProcesses if it's not already there;
                //add criterion to the ucd process if it IS already there
                //TODO: parse applied test task and participant unique IDs
                //TODO: create new task task object(s) with the relevant participants from the master list
                //look for matching task id+participant ids in the master list of test tasks
                //add this criterion to it if it's there; add it to the master list if it's not
            }
            nextCertResultIndex = uploadUtil.getNextIndexOfCertificationResult(
                    nextCertResultIndex + parsedCertResultRecords.size() - 1, headingRecord);
        }

        CertifiedProductSed sed = CertifiedProductSed.builder()
                .testTasks(testTasks)
                .ucdProcesses(ucdProcesses)
            .build();
        return sed;
    }

    private Map<String, Set<String>> parseTestTaskIdsWithParticipantIds(
            CSVRecord certResultHeading, List<CSVRecord> certResultRecords) {
        Map<String, Set<String>> uniqueTaskMaps = new LinkedHashMap<String, Set<String>>();
        List<String> testTaskIds = parseTaskIds(certResultHeading, certResultRecords);
        List<String> testParticipantIds = parseParticipantIds(certResultHeading, certResultRecords);
        if (CollectionUtils.isEmpty(testTaskIds)
                && CollectionUtils.isEmpty(testParticipantIds)) {
            return uniqueTaskMaps;
        }

        int max = 0;
        if (CollectionUtils.isNotEmpty(testTaskIds)) {
            max = Math.max(max, testTaskIds.size());
        }
        if (CollectionUtils.isNotEmpty(testParticipantIds)) {
            max = Math.max(max, testParticipantIds.size());
        }

        IntStream.range(0, max)
                .forEachOrdered(index -> updateUniqueTaskIdMaps(uniqueTaskMaps, index, testTaskIds, testParticipantIds));
        return uniqueTaskMaps;
    }

    private void updateUniqueTaskIdMaps(Map<String, Set<String>> uniqueTaskIdMaps, int index, List<String> testTaskIds,
            List<String> testParticipantIds) {
        String testTaskId = (testTaskIds != null && testTaskIds.size() > index) ? testTaskIds.get(index) : null;
        String testParticipantIdsDelimited = (testParticipantIds != null && testParticipantIds.size() > index)
                ? testParticipantIds.get(index) : null;

        Set<String> participantIds = new HashSet<String>();
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

        if (uniqueTaskIdMaps.containsKey(testTaskId)) {
            Set<String> taskParticipantIds = uniqueTaskIdMaps.get(testTaskId);
            taskParticipantIds.addAll(participantIds);
        } else {
            uniqueTaskIdMaps.put(testTaskId, participantIds);
        }
    }

    private List<String> parseTaskIds(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseMultiRowFieldWithoutEmptyValues(Headings.TASK_ID, certHeadingRecord, certResultRecords);
    }

    private List<String> parseParticipantIds(CSVRecord certHeadingRecord, List<CSVRecord> certResultRecords) {
        return uploadUtil.parseMultiRowFieldWithoutEmptyValues(Headings.PARTICIPANT_ID, certHeadingRecord, certResultRecords);
    }
}
