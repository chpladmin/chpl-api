package gov.healthit.chpl.upload.listing.handler;

import java.util.List;

import javax.validation.ValidationException;

import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import gov.healthit.chpl.upload.listing.ListingUploadHandlerUtil;

@Component("sedUploadHandler")
public class SedUploadHandler {
    private TestTaskUploadHandler testTaskHandler;
    private TestParticipantsUploadHandler testParticipantHandler;
    private UcdProcessUploadHandler ucdHandler;
    private ListingUploadHandlerUtil uploadUtil;

    @Autowired
    public SedUploadHandler(TestTaskUploadHandler testTaskHandler,
            TestParticipantsUploadHandler testParticipantHandler,
            UcdProcessUploadHandler ucdHandler,
            ListingUploadHandlerUtil uploadUtil) {
        this.testTaskHandler = testTaskHandler;
        this.testParticipantHandler = testParticipantHandler;
        this.ucdHandler = ucdHandler;
        this.uploadUtil = uploadUtil;
    }

    public CertifiedProductSed parseAsSed(CSVRecord headingRecord, List<CSVRecord> listingRecords)
        throws ValidationException {
        List<TestTask> parsedTasks = testTaskHandler.handle(headingRecord, listingRecords);
        List<TestParticipant> parsedParticipants = testParticipantHandler.handle(headingRecord, listingRecords);
        //List<UcdProcess> parsedUcdProcesses = ucdHandler.handle(certHeadingRecord, certResultRecords);
        //TODO: go through each set of columns for a criteria
            //look for test tasks
            //look for test participants
            //look for UCD Processes

        CertifiedProductSed sed = CertifiedProductSed.builder()
            .build();
        return sed;
    }
}
