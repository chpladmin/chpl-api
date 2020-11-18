package gov.healthit.chpl.upload.listing.handler;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.domain.UcdProcess;
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
}
