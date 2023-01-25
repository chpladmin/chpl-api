package gov.healthit.chpl.scheduler.job.onetime.participants;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.CertifiedProductSed;
import gov.healthit.chpl.domain.TestParticipant;
import gov.healthit.chpl.domain.TestTask;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2(topic = "updateParticipantsJobLogger")
public class DeduplicationStrategy implements ParticipantUpdateStrategy {

    public boolean updateParticipants(CertifiedProductSearchDetails listing) {
        int numTestTasks = listing.getSed().getTestTasks().size();
        List<TestParticipant> allParticipants = listing.getSed().getTestTasks().stream()
                .flatMap(testTask -> testTask.getTestParticipants().stream())
                .collect(Collectors.toList());

        Iterator<TestTask> taskIter = listing.getSed().getTestTasks().iterator();
        while (taskIter.hasNext()) {
            Iterator<TestParticipant> participantIter = taskIter.next().getTestParticipants().iterator();
            while (participantIter.hasNext()) {
                TestParticipant currParticipant = participantIter.next();
                getAllParticipantsLikeThisParticipant(listing.getSed(), currParticipant);
            }
        }
        //TODO
        // N = # test tasks; check that each participant is duplicated exactly N times
            // if that is not the case then we cannot proceed
        // Reduce each set of N participants to 1 and assign it to the correct test task(s)
        return false;
    }

    private void getAllParticipantsLikeThisParticipant(CertifiedProductSed sed, TestParticipant participant) {

    }
}
